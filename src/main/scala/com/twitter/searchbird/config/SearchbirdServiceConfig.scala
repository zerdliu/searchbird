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
//  var tracerFactory: Tracer.Factory = NullTracer.factory
  var tracerFactory: Tracer.Factory =
    ZipkinTracer(scribeHost="127.0.0.1", scribePort=9410,
      // statsReceiver = NullStatsReceiver,
      sampleRate=1.toFloat)

  def apply(runtime: RuntimeEnvironment) = new SearchbirdServiceImpl(this)
}
