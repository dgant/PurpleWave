package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Companies._
import Micro.Squads.Goals.{Chill, SquadGoal}
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel.EnrichedPixelCollection

import scala.collection.mutable.ArrayBuffer

class Squad(val client: Plan) {
  
  var goal: SquadGoal = Chill
  
  var enemies   : Iterable[UnitInfo]            = Iterable.empty
  var recruits  : ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty
  
  val antiAir       = new AntiAir       (this)
  val antiGround    = new AntiGround    (this)
  val detectors     = new Detectors     (this)
  val healers       = new Healers       (this)
  val repairers     = new Repairers     (this)
  val siege         = new Siege         (this)
  val splashAir     = new SplashAir     (this)
  val splashGround  = new SplashGround  (this)
  val spotters      = new Spotters      (this)
  val transports    = new Transport     (this)
  
  var needsAntiAir      : Boolean = _
  var needsAntiGround   : Boolean = _
  var needsDetectors    : Boolean = _
  var needsHealers      : Boolean = _
  var needsRepairers    : Boolean = _
  var needsBuilders     : Boolean = _
  var needsSplashAir    : Boolean = _
  var needsSplashGround : Boolean = _
  var needsSpotters     : Boolean = _
  var needsTransport    : Boolean = _
  var needsSiege        : Boolean = _
  
  def update() {
    goal.update(this)
  }
  
  def conscript(units: Iterable[FriendlyUnitInfo]) {
    recruits.clear()
    recruits ++= units
    With.squads.commission(this)
    updateNeeds()
  }
  
  def recruit(unit: FriendlyUnitInfo) {
    recruits += unit
    With.squads.addUnit(this, unit)
    updateNeeds()
  }
  
  def updateNeeds() {
    
    def needs(boolean: Boolean) = goal.acceptsHelp
    
    needsAntiAir      = needs(goal.requiresAntiAir      || enemies.exists(_.flying))
    needsAntiGround   = needs(goal.requiresAntiGround   || enemies.exists( ! _.flying))
    needsDetectors    = needs(goal.requiresDetectors    || enemies.exists(e => e.cloaked || e.burrowed || e.is(Zerg.Lurker) || e.is(Terran.Ghost) || e.is(Terran.Wraith) || e.is(Protoss.Arbiter)))
    needsHealers      = needs(goal.requiresHealers      || recruits.exists(_.unitClass.isOrganic))
    needsRepairers    = needs(goal.requiresRepairers    || recruits.exists(_.unitClass.isMechanical))
    needsBuilders     = needs(goal.requiresBuilders)
    needsSplashAir    = needs(goal.requiresSplashAir    || enemies.count(_.flying)     > 3)
    needsSplashGround = needs(goal.requiresSplashGround || enemies.count( ! _.flying)  > 3)
    needsSpotters     = needs(goal.requiresSpotters     || recruits.exists(_.unitClass.isSiegeTank))
    needsTransport    = needs(goal.requiresTransport    || recruits.exists(u => unitsNeedingTransport.contains(u.unitClass)))
    needsSiege        = needs(goal.requiresSiege        || enemies.exists(e => e.unitClass.isStaticDefense || e.unitClass.isSiegeTank))
  }
  
  def centroid: Pixel = {
    if (recruits.isEmpty)
      With.geography.home.pixelCenter
    else
      recruits.map(_.pixelCenter).centroid
  }
  
  private val unitsNeedingTransport = Vector[UnitClass](
    Protoss.HighTemplar,
    Protoss.Reaver,
    Zerg.Defiler
  )
}
