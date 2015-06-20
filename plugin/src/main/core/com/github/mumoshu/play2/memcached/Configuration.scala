package com.github.mumoshu.play2.memcached

import java.util.concurrent.TimeUnit

import net.spy.memcached.transcoders.Transcoder
import play.api.Logger

case class Configuration(configuration: play.api.Configuration) {
  lazy val logger = Logger("memcached.plugin")
  lazy val tc = new CustomSerializing().asInstanceOf[Transcoder[Any]]
  lazy val namespace: String = configuration.getString("memcached.namespace").getOrElse("")
  lazy val timeout: Int = configuration.getInt("memcached.timeout").getOrElse(1)
  lazy val timeunit: TimeUnit = {
    configuration.getString("memcached.timeunit").getOrElse("seconds") match {
      case "seconds" => TimeUnit.SECONDS
      case "milliseconds" => TimeUnit.MILLISECONDS
      case "microseconds" => TimeUnit.MICROSECONDS
      case "nanoseconds" => TimeUnit.NANOSECONDS
      case _ => TimeUnit.SECONDS
    }
  }
  lazy val hashkeys: String = configuration.getString("memcached.hashkeys").getOrElse("off")

  // you may override hash implementation to use more sophisticated hashes, like xxHash for higher performance
  def hash(key: String): String = if(hashkeys == "off") key
    else java.security.MessageDigest.getInstance(hashkeys).digest(key.getBytes).map("%02x".format(_)).mkString
}
