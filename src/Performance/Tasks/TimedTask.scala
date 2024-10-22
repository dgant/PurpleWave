package Performance.Tasks

import Debugging.ToString
import Lifecycle.{Main, With}
import Mathematics.Maff
import Performance.Cache


import scala.collection.mutable

abstract class TimedTask {

  With.performance.addTask(this)

  var skipsMax      : Int     = 24
  var weight        : Int     = 1
  var cosmetic      : Boolean = false
  var alwaysSafe    : Boolean = false
  var name          : String  = ToString(this)
  var lastRunFrame  : Int   = -1

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
  final val runMsRecentMax      = new Cache[Long](() => Maff.max(runMsPast).getOrElse(0))
  final val runMsRecentMean     = new Cache(() => runMsPast.view.map(Math.min(_, 100)).sum / Math.max(1, runMsPast.size))
  final val runMsRecentTotal    = new Cache(() => runMsPast.sum)
  final val runMsSamplesMax     = 8
  private def runMsProjected: Double = Math.max(if (runsTotal < 10) 5 else 1, if (With.performance.disqualificationDanger && Main.jbwapiConfiguration.getAsync) runMsRecentMax() else runMsRecentMean())
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
    val targetAlreadyViolated = With.performance.frameBrokeTarget
    val limitAlreadyViolated  = With.performance.frameBrokeLimit
    onRun(budgetMsCapped)
    val millisecondsAfter     = With.performance.systemMillis
    var millisecondsDuration  = millisecondsAfter - millisecondsBefore

    // Debug pauses (ie. setting breakpoints) produce measurement outliers and throw off performance tuning
    // Detect and ignore debug pauses; use a reasonable default value in their place
    if (With.performance.frameHitBreakpoint) {
      millisecondsDuration = runMsRecentMean()
    }

    _runMsTotal += millisecondsDuration
    if (With.frame > 5) {
      _runMsMax = Math.max(_runMsMax, millisecondsDuration)
    }
    runMsEnqueue(millisecondsDuration)
    if ( ! targetAlreadyViolated && With.performance.frameBrokeTarget) {
      _runsCrossingTarget += 1
    }
    if ( ! limitAlreadyViolated && With.performance.frameBrokeLimit) {
      _runsCrossingLimit += 1
      if (skipsMax > 0 && With.configuration.enablePerformancePauses && With.performance.lastTaskWarningFrame < With.frame) {
        With.performance.lastTaskWarningFrame = With.frame
        With.logger.performance(f"$toString${if(due)" (Due)" else ""} crossed ${With.configuration.frameLimitMs}ms taking ${millisecondsDuration}ms on a ${budgetMs}ms budget (${Maff.meanL(budgetMsPast).toInt}ms avg budget), reaching ${With.performance.frameElapsedMs}ms on the frame.")
      }
    }
    lastRunFrame = With.frame
    _runsTotal += 1
  }

  override def toString: String = name
}
