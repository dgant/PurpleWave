package Information.Battles.Types

import Information.Battles.Prediction.Simulation._
import Information.Battles.Prediction.SimulationCheckpoint
import Information.Geography.Types.Edge
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?
import Utilities.Time.Minutes

import scala.collection.mutable

class Battle(unitsUs: Seq[UnitInfo] = Vector.empty, unitsEnemy: Seq[UnitInfo] = Vector.empty, val isGlobal: Boolean) {
  lazy val frameCreated : Int           = With.frame
  lazy val us           : FriendlyTeam  = new FriendlyTeam(this, unitsUs.flatMap(_.friendly))
  lazy val enemy        : EnemyTeam     = new EnemyTeam(this, unitsEnemy)
  lazy val teams        : Vector[Team]  = Vector(us, enemy)
  lazy val focus        : Pixel         = Maff.centroid(teams.map(_.vanguardAll()))

  def predictionComplete: Boolean = simulationComplete && skimulationComplete

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  //////////////////////////
  // Prediction arguments //
  //////////////////////////

  // Simulation is expensive and gets less accurate as team sizes increase
  lazy val skimWeight: Double = {
    lazy val weight = frameCreated.toDouble / Minutes(20)() - 0.2
    if      (isGlobal)                                  1.0
    else if (With.configuration.simulationAsynchronous) 0.0
    else if (With.reaction.sluggishness > 0)            1.0
    else if (weight > 0.9)                              1.0
    else if (weight < 0.1)                              0.0
    else                                                weight
  }
  lazy val simWeight        : Double                = 1.0 - skimWeight
  lazy val simulated        : Boolean               = simWeight > 0 && ! isGlobal
  lazy val skimulated       : Boolean               = skimWeight > 0
  lazy val logSimulation    : Boolean               = With.configuration.debugging
  lazy val speedMultiplier  : Double                = ?(isGlobal, 1.0      , judgmentModifiers.map(_.speedMultiplier).product)
  lazy val judgmentModifiers: Seq[JudgmentModifier] = ?(isGlobal, Seq.empty, JudgmentModifiers(this))
  lazy val differentialSkim : Double                = us.skimStrengthTotal - enemy.skimStrengthTotal
  def      differential     : Double                = judgement.map(j => skimWeight * differentialSkim + simWeight * j.scoreSim11 * (us.skimStrengthTotal + enemy.skimStrengthTotal) / 2.0).getOrElse(differentialSkim)

  @volatile
  var simulationComplete  : Boolean = ! simulated
  var skimulationComplete : Boolean = ! skimulated

  ////////////////////////
  // Simulation results //
  ////////////////////////

  var judgement: Option[BattleJudgment] = None
  var simulationFrames = 0
  var simulationDeaths = 0.0
  var simulationReport                                                  = new mutable.HashMap[UnitInfo, ReportCard]
  def simulationLog         : String                                    = simulationEvents.map(_.toString).mkString("\n")
  var simulationEvents      : Iterable[SimulationEvent]                 = Iterable.empty
  val simulationCheckpoints : mutable.ArrayBuffer[SimulationCheckpoint] = new mutable.ArrayBuffer[SimulationCheckpoint]

  //////////////
  // Features //
  //////////////

  def choke: Option[Edge] = _choke()
  def scarabTargets: Seq[(UnitInfo, UnitInfo)] = _scarabTargets()

  private val _choke = new Cache[Option[Edge]](() => {
    val pUs   = us.attackCentroidGround
    val pFoe  = enemy.vanguardGround()
    val edge  = Maff.minBy(pUs.zone.edges.filter(_.otherSideof(pUs.zone) == pFoe.zone))(e => e.pixelCenter.pixelDistanceSquared(pUs) + e.pixelCenter.pixelDistanceSquared(pFoe))
    if (pUs.zone == pFoe.zone) None
    else if (edge.isEmpty) None
    else if (pFoe.pixelDistance(edge.get.pixelCenter) + edge.get.radiusPixels < us.maxRangeGround) None
    else edge
  })
  private val _scarabTargets = new Cache[Seq[(UnitInfo, UnitInfo)]](() =>
    teams.flatMap(t =>
      t.units
      .filter(Protoss.Scarab)
      .filter(_.orderTarget.isDefined)
      .map(s => (s, s.orderTarget.get))))

  ///////////////
  // Debugging //
  ///////////////

  def auditSim: Vector[SimulationEvent] = simulationEvents.filter(e =>
    // Only units which are trying to fight
    (e.simulacrum.damageDealt > 0 || e.simulacrum.damageReceived > 0 || e.simulacrum.target.exists(_.isEnemyOf(e.simulacrum.realUnit)))
    // Only events which communicate action
    && (e.isInstanceOf[SimulationEventAttack]
      || e.isInstanceOf[SimulationEventDeath]
      || e.isInstanceOf[SimulationEventTween])).toVector
}
