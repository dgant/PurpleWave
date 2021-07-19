package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Macro.Scheduling.MacroCounter
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.UnitPreference
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Seconds

class TrainUnit(val traineeClass: UnitClass) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def buildable: Buildable = BuildableUnit(traineeClass)

  val currencyLock    = new LockCurrencyForUnit(this, traineeClass)
  val trainerClass    = traineeClass.whatBuilds._1
  val addonsRequired  = traineeClass.buildUnitsEnabling.find(b => b.isAddon && b.whatBuilds._1 == trainerClass)
  val matchTrainer    = MatchAnd(trainerClass, MatchNot(MatchMobileFlying))
  val trainerMatcher  =
    if (addonsRequired.isDefined)
      MatchAnd(matchTrainer, MatchHasAddon(addonsRequired.head))
    else if (traineeClass == Terran.NuclearMissile)
      MatchAnd(matchTrainer, MatchNot(MatchHasNuke))
    else
      matchTrainer
    
  lazy val trainerLock = new LockUnits(this)
  trainerLock.matcher = u => trainerMatcher.apply(u) && (trainer.contains(u) || u.friendly.exists(_.trainee.forall(t => t.is(traineeClass) || t.completeOrNearlyComplete)))
  trainerLock.counter = CountOne
  trainerLock.preference = preference
  
  def trainer: Option[FriendlyUnitInfo] = trainerLock.units.headOption
  def trainee: Option[FriendlyUnitInfo] = trainer.flatMap(_.trainee.filter(traineeClass))
  override def isComplete: Boolean = trainee.exists(t => MacroCounter.countComplete(t)(traineeClass) > 0)
  
  override def onUpdate() {
    if (isComplete) return
    if (400 - With.self.supplyUsed < traineeClass.supplyRequired) return

    trainee.foreach(_.setProducer(this))
  
    // Duplicated across MorphUnit
    currencyLock.framesPreordered = (
      traineeClass.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(trainerClass)
      :+ trainer.map(_.remainingOccupationFrames).getOrElse(0)).max
    currencyLock.isSpent = trainee.isDefined
    currencyLock.acquire()
    if (currencyLock.satisfied) {
      trainerLock.acquire()
      if (trainee.isEmpty && trainer.forall(_.buildUnit.exists( ! _.completeOrNearlyComplete))) {
        // If this trainer is occupied right now, release it,
        // because maybe we can get a free trainer next time
        trainerLock.release()
      }
      if (trainee.isEmpty) {
        trainer.foreach(_.intend(this, new Intention { toTrain = Some(traineeClass) }))
      }
    }
  }

  private val safetyFramesMax = Seconds(10)()
  private lazy val mapSize = With.mapTileWidth + With.mapTileHeight
  private lazy val preference = new UnitPreference {
    override def apply(trainer: FriendlyUnitInfo): Double = {
      // Lower score -> More preferred

      // These factors should produce unambiguous ordering based on factor importance;
      // either they should range on [0, 1] or have discrete { -1, 0, 1 } values

      // Strongly prefer a trainer already making the class,
      // to avoid accidentally picking up and training another unit.
      val already   = trainer.trainee.map(t => if (t.is(traineeClass)) -1.0 else 1.0).getOrElse(0.0)
      val addon     = if (trainer.addon.isDefined) 0.0 else 1.0
      val readiness = trainer.remainingOccupationFrames.toDouble / Math.max(trainer.trainee.map(_.unitClass.buildFrames).getOrElse(0), traineeClass.buildFrames)
      val workers   = if (traineeClass.isWorker) Maff.clamp(trainer.base.map(_.saturation()).getOrElse(0.0), 0.0, 1.0) else 0.0
      val health    = trainer.totalHealth / trainer.unitClass.maxTotalHealth.toDouble
      val safety    = Maff.clamp(trainer.matchups.framesOfSafety, 0, safetyFramesMax) / safetyFramesMax.toDouble
      val distance  = 1.0 - Maff.clamp(trainer.tile.pixelDistanceGround(With.scouting.mostBaselikeEnemyTile) / mapSize, 0.0, 1.0)
      val score = (
          1000000000000.0 * already
        + 10000000000.0   * addon
        + 100000000.0     * workers
        + 1000000.0       * readiness
        + 10000.0         * health
        + 100.0           * safety
        + 1.0             * distance
      )
      score
    }
  }

  override val toString: String = f"Train a $traineeClass"
}
