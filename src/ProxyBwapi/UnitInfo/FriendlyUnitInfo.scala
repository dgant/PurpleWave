package ProxyBwapi.UnitInfo

import Information.Grids.Floody.GridFloody
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.{Agent, Intention}
import Micro.Squads.Squad
import Performance.Cache
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.Forever

import scala.collection.JavaConverters._

final class FriendlyUnitInfo(base: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(base, id) {
  
  override val friendly: Option[FriendlyUnitInfo] = Some(this)

  var lastSetRally                    : Int     = 0
  private var _knownToEnemy           : Boolean = false
  private var _lastFrameOccupied      : Int     = - Forever()
  private var _framesFailingToMove    : Int     = 0
  private var _framesFailingToAttack  : Int     = 0
  override def update() {
    if (frameDiscovered < With.frame) readProxy()
    super.update()
    _knownToEnemy = _knownToEnemy || visibleToOpponents
    lazy val tryingToAttackHere = canAttack && target.exists(t => t.isEnemyOf(this) &&   inRangeToAttack(t))
    lazy val tryingToAttackAway = canAttack && target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    _framesFailingToMove += 1 - PurpleMath.fromBoolean(flying || unitClass.floats || pixel != previousPixel(1) || ! canMove || ( ! agent.tryingToMove && ! tryingToAttackAway))
    _framesFailingToAttack += 1 - PurpleMath.fromBoolean(cooldownLeft > 0 || ! tryingToAttackHere)
    if (remainingOccupationFrames > 0) _lastFrameOccupied = With.frame
  }

  def knownToEnemy  : Boolean = _knownToEnemy
  def seeminglyStuck: Boolean = _framesFailingToMove > 24 || _framesFailingToAttack > 24
  def resetSticking(): Unit = {
    _framesFailingToMove = 0
    _framesFailingToAttack = 0
  }

  def remainingCompletionFrames : Int = bwapiUnit.getRemainingBuildTime
  def spaceRemaining: Int = bwapiUnit.getSpaceRemaining
  def kills: Int = bwapiUnit.getKillCount
  def lastFrameOccupied: Int = _lastFrameOccupied

  lazy val agent: Agent = new Agent(this)
  def intent: Intention = agent.intent

  private var _squad: Option[Squad] = None
  private var _lastSquadChange: Int = 0
  def squad: Option[Squad] = _squad
  def squadAge: Int = With.framesSince(_lastSquadChange)
  def setSquad(newSquad: Option[Squad]): Unit = {
    if (newSquad != _squad) {
      _squad = newSquad
      _lastSquadChange = With.frame
    }
  }

  def alliesSquad                   : Iterable[FriendlyUnitInfo]      = squad.map(_.units.view).map(_.filter(_ != this)).getOrElse(Iterable.empty)
  def alliesBattle                  : Iterable[FriendlyUnitInfo]      = battle.map(_.us.units.view.map(_.friendly).filter(_.nonEmpty).map(_.get)).getOrElse(Iterable.empty).filter(_ != this)
  def alliesAll                     : Iterable[FriendlyUnitInfo]      = With.units.ours.filter(_ != this)
  def enemiesSquad                  : Iterable[UnitInfo]              = squad.map(_.enemies).getOrElse(Iterable.empty)
  def enemiesBattle                 : Iterable[UnitInfo]              = battle.map(_.enemy.units.view).getOrElse(Seq.empty)
  def enemiesAll                    : Iterable[UnitInfo]              = With.units.enemy
  def alliesBattleThenSquad         : Seq[Iterable[FriendlyUnitInfo]] = Seq(alliesBattle, alliesSquad)
  def alliesBattleThenSquadThenAll  : Seq[Iterable[FriendlyUnitInfo]] = Seq(alliesBattle, alliesSquad, alliesAll)
  def alliesSquadThenBattle         : Seq[Iterable[FriendlyUnitInfo]] = Seq(alliesSquad, alliesBattle)
  def alliesSquadThenBattleThenAll  : Seq[Iterable[FriendlyUnitInfo]] = Seq(alliesSquad, alliesBattle, alliesAll)
  def alliesBattleOrSquad           : Iterable[FriendlyUnitInfo]      = if (alliesBattle.nonEmpty) alliesBattle else alliesSquad
  def alliesBattleOrSquadOrAll      : Iterable[FriendlyUnitInfo]      = if (alliesBattle.nonEmpty) alliesBattle else if (alliesSquad.nonEmpty) alliesSquad else alliesAll
  def alliesSquadOrBattle           : Iterable[FriendlyUnitInfo]      = if (alliesSquad.nonEmpty) alliesSquad else alliesBattle
  def alliesSquadOrBattleOrAll      : Iterable[FriendlyUnitInfo]      = if (alliesSquad.nonEmpty) alliesSquad else if (alliesBattle.nonEmpty) alliesBattle else alliesAll
  def enemiesBattleThenSquad        : Seq[Iterable[UnitInfo]]         = Seq(enemiesBattle, enemiesSquad)
  def enemiesBattleThenSquadThenAll : Seq[Iterable[UnitInfo]]         = Seq(enemiesBattle, enemiesSquad, enemiesAll)
  def enemiesSquadThenBattle        : Seq[Iterable[UnitInfo]]         = Seq(enemiesSquad, enemiesBattle)
  def enemiesSquadThenBattleThenAll : Seq[Iterable[UnitInfo]]         = Seq(enemiesSquad, enemiesBattle, enemiesAll)
  def enemiesBattleOrSquad          : Iterable[UnitInfo]              = if (enemiesBattle.nonEmpty) enemiesBattle else enemiesSquad
  def enemiesBattleOrSquadOrAll     : Iterable[UnitInfo]              = if (enemiesBattle.nonEmpty) enemiesBattle else if (enemiesSquad.nonEmpty) enemiesSquad else enemiesAll
  def enemiesSquadOrBattle          : Iterable[UnitInfo]              = if (enemiesSquad.nonEmpty) enemiesSquad else enemiesBattle
  def enemiesSquadOrBattleOrAll     : Iterable[UnitInfo]              = if (enemiesSquad.nonEmpty) enemiesSquad else if (enemiesBattle.nonEmpty) enemiesBattle else enemiesAll
  
  def buildUnit     : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)

  var nextOrderFrame: Option[Int] = None
  def ready: Boolean = nextOrderFrame.forall(_ <= With.frame)
  def unready: Boolean = ! ready
  def sleepUntil(frame: Int): Unit = nextOrderFrame = Some(frame)
  def hijack(): Unit = nextOrderFrame = None

  def trainee: Option[FriendlyUnitInfo] = _traineeCache()
  private val _traineeCache = new Cache[Option[FriendlyUnitInfo]](() =>
    if (training)
      With.units.ours.find(u =>
        ! u.complete
        && u.alive
        && u.pixel == pixel
        && is(u.unitClass.whatBuilds._1))
    else None)

  override def loadedUnitCount: Int = loadedUnits.size
  def loadedUnits: Vector[FriendlyUnitInfo] = _loadedUnitsCache()
  private val _loadedUnitsCache = new Cache(() => if (unitClass.canLoadUnits) base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector else Vector.empty)
  
  def canTransport(passenger: FriendlyUnitInfo): Boolean = (
    isTransport
    && passenger.unitClass.canBeTransported
    && passenger.canMove
    && passenger.transport.forall(_ == this)
    && loadedUnits.view.filterNot(_ == passenger).map(_.unitClass.spaceRequired).sum <= unitClass.spaceProvided)

  def enemyRangeGrid: GridFloody = if (flying || transport.exists(_.flying)) With.grids.enemyRangeAir else With.grids.enemyRangeGround
}
