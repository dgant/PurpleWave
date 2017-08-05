package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Companies._
import Micro.Squads.Goals.{Chill, SquadGoal}
import Planning.Plan
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
  
  var wantAntiAir       : Boolean = _
  var wantAntiGround    : Boolean = _
  var wantDetectors     : Boolean = _
  var needDetectors     : Boolean = _
  var wantHealers       : Boolean = _
  var wantRepairers     : Boolean = _
  var wantSplashAir     : Boolean = _
  var wantSplashGround  : Boolean = _
  var wantSpotters      : Boolean = _
  var wantTransport     : Boolean = _
  var wantSiege         : Boolean = _
  
  def update() {
    goal.update(this)
  }
  
  def conscript(units: Iterable[FriendlyUnitInfo]) {
    recruits.clear()
    recruits ++= units
    With.squads.commission(this)
  }
  
  def recruit(unit: FriendlyUnitInfo) {
    recruits += unit
    With.squads.addUnit(this, unit)
  }
  
  def centroid: Pixel = {
    if (recruits.isEmpty)
      With.geography.home.pixelCenter
    else
      recruits.map(_.pixelCenter).centroid
  }
}
