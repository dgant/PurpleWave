package Utilities.Time

case class Hours(h: Int) extends FrameCount(h * 60 * 60 * 24)
