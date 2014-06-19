package com.twitter.searchbird
package config

import com.twitter.finagle.tracing.{NullTracer, Tracer}
import com.twitter.finagle.zipkin.thrift.{ZipkinTracer}
import com.twitter.logging.Logger
import com.twitter.logging.config._
import com.twitter.ostrich.admin.{RuntimeEnvironment, ServiceTracker}
import com.twitter.ostrich.admin.config._
import com.twitter.util.Config

class SearchbirdServiceConfig extends ServerConfig[SearchbirdService.ThriftServer] {
  var thriftPort: Int = 9999
  var shards: Seq[String] = Seq()
  // var tracerFactory: Tracer.Factory = NullTracer.factory
  var tracerFactory: Tracer.Factory =
    ZipkinTracer(scribeHost="127.0.0.1", scribePort=9410,
      // statsReceiver = NullStatsReceiver,
      sampleRate=1.toFloat)

  //def apply(runtime: RuntimeEnvironment) = new SearchbirdServiceImpl(this, new ResidentIndex)
  def apply(runtime: RuntimeEnvironment) = {
    val index = runtime.arguments.get("shard") match {
      case Some(arg) =>
        val which = arg.toInt
        if (which >= shards.size || which < 0)
          throw new Exception("invalid shard number %d".format(which))
  
        // override with the shard port
        val Array(_, port) = shards(which).split(":")
        thriftPort = port.toInt
  
        new ResidentIndex
  
      case None =>
        require(!shards.isEmpty)
        val remotes = shards map { new RemoteIndex(_) }
    new CompositeIndex(remotes)
  }

    new SearchbirdServiceImpl(this, index)
  }
}
