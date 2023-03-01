package ProxyBwapi.UnitInfo

import Information.Battles.Types.FriendlyTeam
import Information.Grids.Floody.AbstractGridFloody
import Lifecycle.With
import Micro.Agency.{Agent, Intention}
import Performance.Cache
import ProxyBwapi.Orders
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitTracking.IndexedSet
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Tactic.Squads.Squad
import Utilities.Time.Forever

import scala.collection.JavaConverters._
import scala.util.Try

final class FriendlyUnitInfo(base: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(base, id) with Targeter {
  
  override val friendly: Option[FriendlyUnitInfo] = Some(this)

  var lastSetRally                    : Int     = 0
  private var _lastFrameOccupied      : Int     = - Forever()
  private var _framesFailingToMove    : Int     = 0
  private var _framesFailingToAttack  : Int     = 0
  override def update(): Unit = {
    if (frameDiscovered < With.frame) readProxy()
    if (frameDiscovered == With.frame) With.tactics.produce.queue.find(_.expectTrainee(this)).foreach(setProducer)
    super.update()
    lazy val tryingToAttackHere = canAttack && target.exists(t => t.isEnemyOf(this) &&   inRangeToAttack(t))
    lazy val tryingToAttackAway = canAttack && target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    if (flying || unitClass.floats || pixel != previousPixel(1) || ! canMove || ( ! agent.tryingToMove && ! tryingToAttackAway))
      _framesFailingToMove = 0 else _framesFailingToMove += 1
    if (cooldownLeft > 0 || ! tryingToAttackHere) _framesFailingToAttack = 0 else _framesFailingToAttack += 1
    if (remainingOccupationFrames > 0) _lastFrameOccupied = With.frame
    if (order == Orders.HarvestGas || order == Orders.MiningMinerals) orderTarget.foreach(_.lastFrameHarvested = With.frame)
  }

  def seeminglyStuck: Boolean = _framesFailingToMove > 24 || _framesFailingToAttack > Math.max(24, cooldownMaxAirGround + 2)
  def resetSticking(): Unit = {
    _framesFailingToMove = 0
    _framesFailingToAttack = 0
  }

  def remainingCompletionFrames : Int = bwapiUnit.getRemainingBuildTime
  def lastFrameOccupied: Int = _lastFrameOccupied

  lazy val agent: Agent = new Agent(this)
  private var _client: Any = None
  private var _intent: Intention = new Intention
  def client: Any = _client
  def intent: Intention = _intent
  def intend(intendingClient: Any, intent: Intention = new Intention): Intention = {
    _client = intendingClient
    _intent = intent
    _intent
  }

  private var _squad: Option[Squad] = None
  private var _lastSquadChange: Int = 0
  def friendlyTeam: Option[FriendlyTeam] = team.flatMap(t => Try(t.asInstanceOf[FriendlyTeam]).toOption)
  def squad: Option[Squad] = _squad
  def squadAge: Int = With.framesSince(_lastSquadChange)
  def setSquad(newSquad: Option[Squad]): Unit = {
    if (newSquad != _squad) {
      _squad = newSquad
      _lastSquadChange = With.frame
    }
  }
  def targetsAssigned: Option[IndexedSet[UnitInfo]] = intent.targets.orElse(squad.flatMap(_.targets))

  def alliesSquad   : Iterable[FriendlyUnitInfo]  = squad.map(_.units.view).map(_.filter(_ != this)).getOrElse(Iterable.empty)
  def alliesBattle  : Iterable[FriendlyUnitInfo]  = team.map(_.units.view.map(_.friendly).filter(_.nonEmpty).map(_.get)).getOrElse(Iterable.empty).filter(_ != this)
  def enemiesSquad  : Iterable[UnitInfo]          = squad.map(s => s.targets.getOrElse(s.enemies)).getOrElse(Iterable.empty)
  def enemiesBattle : Iterable[UnitInfo]          = battle.map(_.enemy.units.view).getOrElse(Iterable.empty)

  def buildUnit     : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)

  var nextOrderFrame: Option[Int] = None
  def ready: Boolean = nextOrderFrame.forall(_ <= With.frame)
  def unready: Boolean = ! ready
  def sleepUntil(frame: Int): Unit = nextOrderFrame = Some(frame)

  def trainee: Option[FriendlyUnitInfo] = _traineeCache()
  private val _traineeCache = new Cache(() => if (training) With.units.ours.find(u => ! u.complete && u.pixel == pixel && is(u.unitClass.whatBuilds._1)) else None)

  override def loadedUnitCount: Int = loadedUnits.size
  def loadedUnits: Vector[FriendlyUnitInfo] = _loadedUnitsCache()
  private val _loadedUnitsCache = new Cache(() =>
         if (is(Protoss.Carrier))     interceptors.flatMap(_.friendly).toVector
    else if (unitClass.canLoadUnits)  base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector
    else Vector.empty)
  def canTransport(passenger: FriendlyUnitInfo): Boolean = (
    isTransport
    && passenger.unitClass.canBeTransported
    && passenger.canMove
    && passenger.transport.forall(==)
    && bwapiUnit.getSpaceRemaining + passenger.unitClass.spaceRequired <= unitClass.spaceProvided)

  def enemyRangeGrid: AbstractGridFloody = if (flying || transport.exists(_.flying)) With.grids.enemyRangeAir else With.grids.enemyRangeGround
}
