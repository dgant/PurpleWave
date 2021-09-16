package Utilities.Time

case class GameTime(m: Int, s: Int) extends FrameCount(24 * (60 * m + s))