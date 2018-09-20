package com.github.mumoshu.play2.memcached

import net.spy.memcached.compat.log.{Level, AbstractLogger}
import play.api.Logger

class Slf4JLogger(name: String) extends AbstractLogger(name) {

  val logger = Logger("memcached")

  def isTraceEnabled = logger.isTraceEnabled

  def isDebugEnabled = logger.isDebugEnabled

  def isInfoEnabled = logger.isInfoEnabled

  def log(level: Level, msg: AnyRef, throwable: Throwable) {
    val message = msg.toString
    level match {
      case Level.TRACE => logger.trace(message, throwable)
      case Level.DEBUG => logger.debug(message, throwable)
      case Level.INFO => logger.info(message, throwable)
      case Level.WARN => logger.warn(message, throwable)
      case Level.ERROR => logger.error(message, throwable)
      case Level.FATAL => logger.error("[FATAL] " + message, throwable)
    }
  }
}
