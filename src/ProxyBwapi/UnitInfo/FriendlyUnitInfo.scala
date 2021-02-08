package ProxyBwapi.UnitInfo

import Information.Grids.Combat.AbstractGridEnemyRange
import Lifecycle.With
import Micro.Agency.Agent
import Micro.Squads.Goals.SquadGoal
import Micro.Squads.Squad
import Performance.Cache
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.Forever

import scala.collection.JavaConverters._

class FriendlyUnitInfo(base: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(base, id) {
  
  override val friendly: Option[FriendlyUnitInfo] = Some(this)

  var lastSetRally                    : Int           = 0
  private var _discoveredByEnemy      : Boolean       = false
  private var _lastFrameOccupied      : Int           = - Forever()
  private var _framesFailingToMove    : Int           = 0
  private var _framesFailingToAttack  : Int           = 0
  private var _lastSquadChange        : Int           = 0
  private var _lastSquad              : Option[Squad] = None
  override def update() {
    if (frameDiscovered < With.frame) readProxy()
    super.update()
    _discoveredByEnemy = _discoveredByEnemy || visibleToOpponents
    lazy val tryingToAttackHere = canAttack && target.exists(t => t.isEnemyOf(this) &&   inRangeToAttack(t))
    lazy val tryingToAttackAway = canAttack && target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    _framesFailingToMove = if (flying || unitClass.floats || velocityX > 0 || velocityY > 0 || ! canMove || ( ! agent.tryingToMove && ! tryingToAttackAway)) 0 else _framesFailingToMove + 1
    _framesFailingToAttack = if (cooldownLeft > 0 || ! tryingToAttackHere) 0 else _framesFailingToAttack + 1
    if (remainingOccupationFrames > 0) _lastFrameOccupied = With.frame
  }
  @inline final def seeminglyStuck: Boolean = _framesFailingToMove > 24 || _framesFailingToAttack > 24

  def remainingCompletionFrames : Int = bwapiUnit.getRemainingBuildTime
  def spaceRemaining: Int = bwapiUnit.getSpaceRemaining
  def kills: Int = bwapiUnit.getKillCount
  def lastFrameOccupied: Int = _lastFrameOccupied
  def framesFailingToMove: Int = _framesFailingToMove
  def squad: Option[Squad] = _squadCache()
  def goal: Option[SquadGoal] = squad.map(_.goal)
  def squadmates: Set[FriendlyUnitInfo] = squad.map(_.units).getOrElse(Set.empty)
  def squadenemies: Seq[UnitInfo] = squad.map(_.enemies).getOrElse(Seq.empty)
  def teammates: Set[UnitInfo] = _teammatesCache()
  def enemies: Seq[UnitInfo] = _enemiesCache()
  def immediateAllies: Iterable[UnitInfo] = if (battle.isDefined) matchups.allies else squadmates.view.filterNot(_ == this)
  def immediateOthers: Iterable[UnitInfo] = if (battle.isDefined) matchups.others else squadmates.view.filterNot(_ == this)
  lazy val agent: Agent = new Agent(this)
  private val _squadCache = new Cache(() => {
    val nextSquad = With.squads.all.find(_.units.contains(this))
    if (nextSquad != _lastSquad) { _lastSquadChange = With.frame }
    _lastSquad = nextSquad
    _lastSquad
  })
  private val _teammatesCache = new Cache(() => (squadmates ++ matchups.allies))
  private val _enemiesCache = new Cache(() => (squadenemies ++ matchups.enemies).distinct)
  
  def buildUnit     : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)
  def knownToEnemy: Boolean = _discoveredByEnemy

  var nextOrderFrame: Option[Int] = None
  @inline def ready: Boolean = nextOrderFrame.forall(_ <= With.frame)
  @inline def unready: Boolean = ! ready
  @inline def sleepUntil(frame: Int): Unit = nextOrderFrame = Some(frame)
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
  
  def loadedUnits: Vector[FriendlyUnitInfo] = _loadedUnitsCache()
  private val _loadedUnitsCache = new Cache(() => if (unitClass.canLoadUnits) base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector else Vector.empty)
  
  def canTransport(passenger: FriendlyUnitInfo): Boolean = (
    isTransport
    && passenger.unitClass.canBeTransported
    && passenger.canMove
    && passenger.transport.forall(_ == this)
    && loadedUnits.view.filterNot(_ == passenger).map(_.unitClass.spaceRequired).sum <= unitClass.spaceProvided)

  def enemyRangeGrid: AbstractGridEnemyRange = if (flying || transport.exists(_.flying)) With.grids.enemyRangeAir else With.grids.enemyRangeGround
}
