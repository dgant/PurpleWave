package Performance.Tasks

import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Utilities.ByOption

import scala.collection.mutable

abstract class TimedTask {

  With.performance.addTask(this)

  var skipsMax    : Int     = 48
  var weight      : Int     = 1
  var cosmetic    : Boolean = false
  var alwaysSafe  : Boolean = false
  var name        : String = if (getClass.getSimpleName.contains("anon")) getClass.getSuperclass.getSimpleName else getClass.getSimpleName

  private var lastRunFrame        : Int   = -1
  private var _runsTotal          : Int   = 0
  private var _skipsTotal         : Int   = 0
  private var _runMsTotal         : Long  = 0
  private var _runMsMax           : Long  = 0
  private var _runsCrossingTarget : Int   = 0
  private var _runsCrossingLimit  : Int   = 0
  private val runMs = new mutable.Queue[Long]

  final def due                 : Boolean = framesSinceRunning > skipsMax
  final def framesSinceRunning  : Int     = With.framesSince(lastRunFrame)
  final def hasNeverRun         : Boolean = runsTotal == 0
  final def runsTotal           : Int     = _runsTotal
  final def runsCrossingTarget  : Int     = _runsCrossingTarget
  final def runsCrossingLimit   : Int     = _runsCrossingLimit
  final def runMsTotal          : Long    = _runMsTotal
  final def runMsMax            : Long    = _runMsMax
  final def runMsLast           : Long    = runMs.headOption.getOrElse(0L)
  final val runMsRecentMax  = new Cache[Long](() => ByOption.max(runMs).getOrElse(0))
  final val runMsRecentMean = new Cache(() => runMs.view.map(Math.min(_, 100)).sum / Math.max(1, runMs.size))
  final def runMsProjected: Double = Math.max(if (runsTotal < 10) 5 else 1, if (With.performance.danger) runMsMax else runMsRecentMax())
  final def withSkipsMax    (value: Int)      : TimedTask = { skipsMax    = value; this }
  final def withWeight      (value: Int)      : TimedTask = { weight      = value; this }
  final def withCosmetic    (value: Boolean)  : TimedTask = { cosmetic    = value; this }
  final def withAlwaysSafe  (value: Boolean)  : TimedTask = { alwaysSafe  = value; this }
  final def withName        (value: String)   : TimedTask = { name        = value; this }

  protected def onRun(budgetMs: Long)

  def isComplete: Boolean = true

  final def safeToRun(budgetMs: Long): Boolean = (
    hasNeverRun
    || framesSinceRunning > skipsMax
    || budgetMs > runMsProjected
    || ! With.configuration.enablePerformancePauses)

  final def run(budgetMs: Long) {
    val budgetMsCapped        = Math.min(budgetMs, With.performance.millisecondsUntilTarget)
    val millisecondsBefore    = With.performance.systemMillis
    val targetAlreadyViolated = With.performance.violatedTarget
    val limitAlreadyViolated  = With.performance.violatedLimit
    onRun(budgetMsCapped)
    val millisecondsAfter     = With.performance.systemMillis
    var millisecondsDuration  = millisecondsAfter - millisecondsBefore

    // Debug pauses (ie. setting breakpoints) produce measurement outliers and throw off performance tuning
    // Detect and ignore debug pauses; use a reasonable default value in their place
    if (With.configuration.debugging && millisecondsDuration >= With.configuration.debugPauseThreshold) {
      millisecondsDuration = runMsRecentMean()
    }

    _runMsTotal += millisecondsDuration
    if (With.frame > 5) {
      _runMsMax = Math.max(_runMsMax, millisecondsDuration)
    }
    runMs.enqueue(Math.max(0L, millisecondsDuration))
    while (runMs.size > 10) {
      runMs.dequeue()
    }
    _runsCrossingTarget += PurpleMath.fromBoolean( ! targetAlreadyViolated && With.performance.violatedTarget)
    if ( ! limitAlreadyViolated && With.performance.violatedLimit) {
      _runsCrossingLimit += 1
      if (skipsMax > 0 && With.performance.enablePerformancePauses) {
        With.logger.warn(f"$toString${if(due)" (Due)" else ""} crossed the ${With.configuration.frameMillisecondLimit}ms limit, taking ${millisecondsDuration}ms, reaching {With.performance.millisecondsSpentThisFrame}ms on the frame.")
      }
    }
    lastRunFrame = With.frame
    _runsTotal += 1
    if (With.configuration.logTaskDuration) {
      With.logger.debug(f"$toString duration: ${runMsLast}ms")
    }
  }

  override def toString: String = name
}
