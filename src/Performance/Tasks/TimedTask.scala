package Performance.Tasks

import Debugging.ToString
import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Utilities.ByOption

import scala.collection.mutable

abstract class TimedTask {

  With.performance.addTask(this)

  var skipsMax    : Int     = 24
  var weight      : Int     = 1
  var cosmetic    : Boolean = false
  var alwaysSafe  : Boolean = false
  var name        : String  = ToString(this)

  private var lastRunFrame        : Int   = -1
  private var _runsTotal          : Int   = 0
  private var _skipsTotal         : Int   = 0
  private var _runMsTotal         : Long  = 0
  private var _runMsMax           : Long  = 0
  private var _runsCrossingTarget : Int   = 0
  private var _runsCrossingLimit  : Int   = 0
  private val runMsPast = new mutable.Queue[Long]
  val budgetMsPast = new mutable.Queue[Long]

  final def withSkipsMax    (value: Int)      : TimedTask = { skipsMax    = value; this }
  final def withWeight      (value: Int)      : TimedTask = { weight      = value; this }
  final def withCosmetic    (value: Boolean)  : TimedTask = { cosmetic    = value; this }
  final def withAlwaysSafe  (value: Boolean)  : TimedTask = { alwaysSafe  = value; this }
  final def withName        (value: String)   : TimedTask = { name        = value; this }
  final def due                 : Boolean = framesSinceRunning > skipsMax
  final def framesSinceRunning  : Int     = With.framesSince(lastRunFrame)
  final def hasNeverRun         : Boolean = runsTotal == 0
  final def runsTotal           : Int     = _runsTotal
  final def runsCrossingTarget  : Int     = _runsCrossingTarget
  final def runsCrossingLimit   : Int     = _runsCrossingLimit
  final def runMsTotal          : Long    = _runMsTotal
  final def runMsMax            : Long    = _runMsMax
  final def runMsLast           : Long    = runMsPast.headOption.getOrElse(0L)
  final def runMsRecentSamples  : Int     = runMsPast.length
  final val runMsRecentMax      = new Cache[Long](() => ByOption.max(runMsPast).getOrElse(0))
  final val runMsRecentMean     = new Cache(() => runMsPast.view.map(Math.min(_, 100)).sum / Math.max(1, runMsPast.size))
  final val runMsRecentTotal    = new Cache(() => runMsPast.sum)
  final val runMsSamplesMax     = 24
  private def runMsProjected: Double = Math.max(if (runsTotal < 10) 5 else 1, if (With.performance.danger) runMsMax else runMsRecentMax())
  private def runMsEnqueue(value: Long): Unit = {
    runMsPast.enqueue(Math.max(0L, value))
    while (runMsPast.size > runMsSamplesMax) runMsPast.dequeue()
  }
  private def budgetMsEnqueue(value: Long): Unit = {
    budgetMsPast.enqueue(value)
    while (budgetMsPast.size > runMsSamplesMax) budgetMsPast.dequeue()
  }

  protected def onRun(budgetMs: Long)

  def isComplete: Boolean = true

  final def safeToRun(budgetMs: Long): Boolean = (
    alwaysSafe
    || hasNeverRun
    || due
    || budgetMs >= runMsProjected
    || ! With.configuration.enablePerformancePauses)

  final def skip(): Unit = {
    runMsEnqueue(0)
  }

  final def run(budgetMs: Long) {
    budgetMsEnqueue(budgetMs)
    val budgetMsCapped        = Math.min(budgetMs, With.performance.msBeforeTarget)
    val millisecondsBefore    = With.performance.systemMillis
    val targetAlreadyViolated = With.performance.violatedTarget
    val limitAlreadyViolated  = With.performance.violatedLimit
    onRun(budgetMsCapped)
    val millisecondsAfter     = With.performance.systemMillis
    var millisecondsDuration  = millisecondsAfter - millisecondsBefore

    // Debug pauses (ie. setting breakpoints) produce measurement outliers and throw off performance tuning
    // Detect and ignore debug pauses; use a reasonable default value in their place
    if (With.performance.hitBreakpointThisFrame) {
      millisecondsDuration = runMsRecentMean()
    }

    _runMsTotal += millisecondsDuration
    if (With.frame > 5) {
      _runMsMax = Math.max(_runMsMax, millisecondsDuration)
    }
    runMsEnqueue(millisecondsDuration)
    if ( ! targetAlreadyViolated && With.performance.violatedTarget) {
      _runsCrossingTarget += 1
    }
    if ( ! limitAlreadyViolated && With.performance.violatedLimit) {
      _runsCrossingLimit += 1
      if (skipsMax > 0 && With.configuration.enablePerformancePauses && With.performance.lastTaskWarningFrame < With.frame) {
        With.performance.lastTaskWarningFrame = With.frame
        With.logger.warn(f"$toString${if(due)" (Due)" else ""} crossed ${With.configuration.frameLimitMs}ms limit on ${budgetMs}ms budget (${PurpleMath.meanL(budgetMsPast).toInt}ms avg budget), taking ${millisecondsDuration}ms, reaching ${With.performance.frameMs}ms on the frame.")
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
