package com.twitter.searchbird

import com.twitter.conversions.time._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import java.net.InetSocketAddress
import scala.tools.nsc.interpreter._
import scala.tools.nsc.Settings
import com.twitter.finagle.zipkin.thrift.{ZipkinTracer}

object SearchbirdConsoleClient extends App {
  val service = ClientBuilder()
    .hosts(new InetSocketAddress(args(0), args(1).toInt))
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .tcpConnectTimeout(3.seconds)
    .tracerFactory(ZipkinTracer(scribeHost="127.0.0.1", scribePort=9410, sampleRate=1.toFloat))
    .build()

  val client = new SearchbirdService.FinagledClient(service)

  val intLoop = new ILoop()

  Console.println("'client' is bound to your thrift client.")
  intLoop.setPrompt("\nfinagle-client> ")

  intLoop.settings = {
    val s = new Settings(Console.println)
    s.embeddedDefaults[SearchbirdService.FinagledClient]
    s.Yreplsync.value = true
    s
  }

  intLoop.createInterpreter()
  intLoop.in = new JLineReader(new JLineCompletion(intLoop))

  intLoop.intp.beQuietDuring {
    intLoop.intp.interpret("""def exit = println("Type :quit to resume program execution.")""")
    intLoop.intp.bind(NamedParam("client", client))
  }

  intLoop.loop()
  intLoop.closeInterpreter()
}
