package trafficland.opensource.sbt.plugins.git

import sbt.{ControlEvent, LogEvent, Level, BasicLogger}

object NullLogger extends BasicLogger {
  def trace(t: => Throwable) {}
  def log(level: Level.Value, message: => String) {}
  def logAll(events: Seq[LogEvent]) {}
  def success(message: => String) {}
  def control(event: ControlEvent.Value, message: => String) {}
}