package Information.StrategyDetection

case class GameTime(minutes: Int, seconds: Int) {
 implicit def frames: Int = 24 * (60 * minutes + seconds)
}
