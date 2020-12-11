package Utilities

case class Seconds(seconds: Int) extends AbstractGameTime(seconds / 60, seconds % 60)
