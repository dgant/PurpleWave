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

  var framesFailingToMove       : Int = 0
  var framesFailingToAttack     : Int = 0
  var lastFrameOccupied         : Int = - Forever()
  override def update() {
    if (frameDiscovered < With.frame) readProxy()
    super.update()
    _discoveredByEnemy = _discoveredByEnemy || visibleToOpponents
    lazy val tryingToAttackHere = canAttack && target.exists(t => t.isEnemyOf(this) &&   inRangeToAttack(t))
    lazy val tryingToAttackAway = canAttack && target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    framesFailingToMove = if (flying || unitClass.floats || velocityX > 0 || velocityY > 0 || ! canMove || ( ! agent.tryingToMove && ! tryingToAttackAway)) 0 else framesFailingToMove + 1
    framesFailingToAttack = if (cooldownLeft > 0 || ! tryingToAttackHere) 0 else framesFailingToAttack + 1
    if (remainingOccupationFrames > 0) lastFrameOccupied = With.frame
  }
  @inline final def seeminglyStuck: Boolean = framesFailingToMove > 24 || framesFailingToAttack > 24

  def remainingCompletionFrames : Int = bwapiUnit.getRemainingBuildTime
  def spaceRemaining: Int = bwapiUnit.getSpaceRemaining
  def kills: Int = bwapiUnit.getKillCount
  
  def squad: Option[Squad] = squadCache()
  def goal: Option[SquadGoal] = squad.map(_.goal)
  def squadmates: Set[FriendlyUnitInfo] = squad.map(_.units).getOrElse(Set.empty)
  def squadenemies: Seq[UnitInfo] = squad.map(_.enemies).getOrElse(Seq.empty)
  def teammates: Set[UnitInfo] = teammatesCache()
  def enemies: Seq[UnitInfo] = enemiesCache()
  def immediateAllies: Iterable[UnitInfo] = if (battle.isDefined) matchups.allies else squadmates.view.filterNot(_ == this)
  def immediateOthers: Iterable[UnitInfo] = if (battle.isDefined) matchups.others else squadmates.view.filterNot(_ == this)
  lazy val agent: Agent = new Agent(this)

  private val squadCache = new Cache(() => With.squads.all.find(_.units.contains(this)))
  private val teammatesCache = new Cache(() => (squadmates ++ matchups.allies))
  private val enemiesCache = new Cache(() => (squadenemies ++ matchups.enemies).distinct)

  var lastSetRally: Int = 0
  
  def buildUnit     : Option[UnitInfo]  = With.units.get(base.getBuildUnit)
  def techingType   : Tech              = Techs.get(base.getTech)
  def upgradingType : Upgrade           = Upgrades.get(base.getUpgrade)
  private var _discoveredByEnemy: Boolean = false
  def knownToEnemy: Boolean = _discoveredByEnemy

  var nextOrderFrame: Option[Int] = None
  @inline def ready: Boolean = nextOrderFrame.forall(_ <= With.frame)
  @inline def unready: Boolean = ! ready
  @inline def sleepUntil(frame: Int): Unit = nextOrderFrame = Some(frame)
  def hijack(): Unit = nextOrderFrame = None

  def trainee: Option[FriendlyUnitInfo] = traineeCache()
  private val traineeCache = new Cache[Option[FriendlyUnitInfo]](() =>
    if (training)
      With.units.ours.find(u =>
        ! u.complete
        && u.alive
        && u.pixel == pixel
        && is(u.unitClass.whatBuilds._1))
    else None)
  
  def loadedUnits: Vector[FriendlyUnitInfo] = loadedUnitsCache()
  private val loadedUnitsCache = new Cache(() =>
    if (unitClass.canLoadUnits)
      base.getLoadedUnits.asScala.flatMap(With.units.get).flatMap(_.friendly).toVector
    else
      Vector.empty)
  
  def canTransport(passenger: FriendlyUnitInfo): Boolean =
    isTransport                           &&
    passenger.unitClass.canBeTransported  &&
    passenger.canMove                     &&
    passenger.transport.forall(_ == this) &&
    loadedUnits.view.filterNot(_ == passenger).map(_.unitClass.spaceRequired).sum <= unitClass.spaceProvided

  def enemyRangeGrid: AbstractGridEnemyRange =
    if (flying || transport.exists(_.flying))
      With.grids.enemyRangeAir
    else
      With.grids.enemyRangeGround
}
