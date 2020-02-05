package Performance

import Lifecycle.With

import scala.collection.mutable

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)

  val frameLimitShort: Int = 85
  
  private var millisecondsBefore = 0l
  private var lastFrameDelayUpdate = 0
  
  var framesOverShort = 0
  var framesOver1000  = 0
  var framesOver10000 = 0

  def enablePerformancePauses: Boolean = With.configuration.enablePerformancePauses // For disabling performance stops while debugging

  var lastUniqueUnitIdCount: Int = 0
  var lastUniqueDeadIdCount: Int = 0
  var lastUniqueFriendlyUnitObjects: Int = 0
  private var uniqueFriendlyUnitObjects: Int = 0
  private val uniqueFriendlyUnitIds = new mutable.HashSet[Int]
  private val uniqueFriendlyDeadIds = new mutable.HashSet[Int]
  def trackUnit(id: Int, alive: Boolean) {
    uniqueFriendlyUnitObjects += 1
    uniqueFriendlyUnitIds += id
    if ( ! alive) {
      uniqueFriendlyDeadIds += id
    }
  }

  def startFrame() {
    millisecondsBefore = System.currentTimeMillis()
  }

  def endFrame() {
    lastUniqueUnitIdCount = uniqueFriendlyUnitIds.size
    lastUniqueDeadIdCount = uniqueFriendlyDeadIds.size
    lastUniqueFriendlyUnitObjects = uniqueFriendlyUnitObjects
    uniqueFriendlyUnitObjects = 0
    uniqueFriendlyUnitIds.clear()
    uniqueFriendlyDeadIds.clear()
    var millisecondDifference = millisecondsSpentThisFrame
    if (With.configuration.debugging && millisecondDifference > With.configuration.debugPauseThreshold) {
      millisecondDifference = meanFrameMilliseconds
    }

    frameTimes(With.frame % framesToTrack) = millisecondDifference
    if (millisecondDifference >= frameLimitShort) framesOverShort += 1
    if (millisecondDifference >= 1000)            framesOver1000  += 1
    if (millisecondDifference >= 10000)           framesOver10000 += 1
  }

  def millisecondsLeftThisFrame: Long = {
    Math.max(0, With.configuration.targetFrameDurationMilliseconds - millisecondsSpentThisFrame)
  }

  def millisecondsSpentThisFrame: Long = {
    Math.max(0, System.currentTimeMillis - millisecondsBefore)
  }

  def continueRunning: Boolean = {
    With.frame == 0 || millisecondsLeftThisFrame > 1 || ! enablePerformancePauses
  }

  def violatedThreshold: Boolean = {
    With.frame > 0 && millisecondsLeftThisFrame <= 0
  }

  def violatedRules: Boolean = {
    With.frame > 0 && millisecondsSpentThisFrame >= frameLimitShort
  }

  def danger: Boolean = (
    With.configuration.enablePerformancePauses
    && (
      framesOverShort > 160 ||
      framesOver1000  > 5   ||
      framesOver10000 > 1)
  )

  def maxFrameMilliseconds  : Long = frameTimes.max
  def meanFrameMilliseconds : Long = frameTimes.view.map(Math.min(_, 100)).sum / framesToTrack

  def disqualified: Boolean =
    framesOverShort >= 320 ||
    framesOver1000  >= 10
}
