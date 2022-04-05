package Lifecycle

import Debugging.Visualizations.{Animations, Viewport, Visualization}
import Debugging.{Camera, LambdaQueue, Logger, Storyteller}
import Information.Accounting
import Information.Battles.Battles
import Information.Battles.Prediction.Simulation.Simulation
import Information.Fingerprinting.Fingerprints
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Grids.Grids
import Information.Scouting.{Scouting, UnitsShown}
import Macro.Allocation._
import Macro.Architecture.Architecture
import Macro.Gathering
import Macro.MacroSim.MacroSim
import Macro.Scheduling.{MacroCounts, ProductionHistory, Projections, Scheduler}
import Mathematics.Points.Tile
import Micro.Agency.Agency
import Micro.Coordination.Coordinator
import Micro.Matchups.MatchupGraph
import NeoGeo.{MapIdentifier, NeoGeo}
import Performance.TaskQueue.{TaskQueueGlobal, TaskQueueParallel}
import Placement.Preplacement
import Planning.{Blackboard, Yolo}
import ProxyBwapi.Bullets.Bullets
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.UnitTracking.UnitTracker
import Strategery.History.History
import Strategery.Strategist
import Tactic.Squads.Squads
import Tactic.Tactician
import _root_.Performance.{Latency, PerformanceMonitor, ReactionTimes}
import bwapi.Flag
import bwta.BWTA

import scala.collection.JavaConverters._

object With {

  var frame             : Int                 = 0
  var self              : PlayerInfo          = _
  var neutral           : PlayerInfo          = _
  var enemies           : Vector[PlayerInfo]  = _
  var mapFileName       : String              = _
  var mapCleanName      : String              = _
  var mapClock          : String              = _
  var startNanoTime     : Long                = 0
  var mapTileWidth      : Int                 = 0
  var mapTileHeight     : Int                 = 0
  var mapTileArea       : Int                 = 0
  var mapPixelWidth     : Int                 = 0
  var mapPixelHeight    : Int                 = 0
  var mapPixelPerimeter : Int                 = 0
  var mapWalkWidth      : Int                 = 0
  var mapWalkHeight     : Int                 = 0
  var mapWalkArea       : Int                 = 0

  var game              : bwapi.Game          = _
  var agents            : Agency              = _
  var animations        : Animations          = _
  var architecture      : Architecture        = _
  var bank              : Bank                = _
  var blackboard        : Blackboard          = _
  var battles           : Battles             = _
  var productionHistory : ProductionHistory   = _
  var bullets           : Bullets             = _
  var bwapiData         : BwapiData           = _
  var camera            : Camera              = _
  var coordinator       : Coordinator         = _
  var configuration     : Configuration       = _
  var accounting        : Accounting          = _
  var fingerprints      : Fingerprints        = _
  var gathering         : Gathering           = _
  var geo               : NeoGeo              = _
  var geography         : Geography           = _
  var grids             : Grids               = _
  var groundskeeper     : Groundskeeper       = _
  var history           : History             = _
  var lambdas           : LambdaQueue         = _
  var latency           : Latency             = _
  var logger            : Logger              = _
  var macroCounts       : MacroCounts         = _
  var macroSim          : MacroSim            = _
  var manners           : Manners             = _
  var matchups          : MatchupGraph        = _
  var paths             : Paths               = _
  var performance       : PerformanceMonitor  = _
  var placement         : PlacementCycle      = _
  var preplacement      : Preplacement        = _
  var projections       : Projections         = _
  var proxy             : ProxyBWAPI          = _
  var priorities        : Priorities         = _
  var reaction          : ReactionTimes       = _
  var recruiter         : Recruiter           = _
  var scheduler         : Scheduler           = _
  var scouting          : Scouting            = _
  var simulation        : Simulation          = _
  var strategy          : Strategist          = _
  var storyteller       : Storyteller         = _
  var squads            : Squads              = _
  var tactics           : Tactician             = _
  var tasks             : TaskQueueParallel   = _
  var units             : UnitTracker         = _
  var unitsShown        : UnitsShown          = _
  var viewport          : Viewport            = _
  var visualization     : Visualization       = _
  var yolo              : Yolo                = _

  def enemy: PlayerInfo = enemies.head
  def framesSince(previousFrame: Int): Int = Math.max(0, frame - previousFrame)
  def framesUntil(futureFrame: Int): Int = Math.max(0, futureFrame - frame)

  def onFrame() {
    frame = With.game.getFrameCount
  }

  def onStart() {
    startNanoTime = System.nanoTime()
    game = JBWAPIClient.getGame
    game.enableFlag(Flag.UserInput)
    game.setLatCom(false)
    game.setLocalSpeed(0)

    ////////////////
    // Basic data //
    ////////////////

    frame             = 0
    proxy             = new ProxyBWAPI
    self              = Players.get(game.self)
    neutral           = Players.get(game.neutral)
    enemies           = game.enemies.asScala.map(Players.get).toVector
    mapFileName       = game.mapFileName
    mapCleanName      = MapIdentifier.apply(mapFileName)
    mapClock          = MapIdentifier.clock(new Tile(With.game.self.getUnits.asScala.maxBy(_.getHitPoints).getTilePosition).center)
    mapTileWidth      = game.mapWidth
    mapTileHeight     = game.mapHeight
    mapTileArea       = mapTileWidth * mapTileHeight
    mapPixelWidth     = mapTileWidth * 32
    mapPixelHeight    = mapTileHeight * 32
    mapPixelPerimeter = 2 * mapPixelWidth + 2 * mapPixelHeight
    mapWalkWidth      = mapTileWidth * 4
    mapWalkHeight     = mapTileHeight * 4
    mapWalkArea       = mapWalkWidth * mapWalkHeight

    ///////////////////
    // Configuration //
    ///////////////////

    bwapiData         = new BwapiData
    configuration     = new Configuration
    logger            = new Logger
    ConfigurationLoader.load()

    ////////////////////
    // Normal systems //
    ////////////////////

    //geo = new NeoGeo(game)
    analyzeTerrain()

    if (With.configuration.visualizeDebug) {
      //NeoRender(geo)
    }

    // Order-dependent initialization:
    // PerformanceMonitor must exist when creating any task
    performance       = new PerformanceMonitor

    agents            = new Agency
    animations        = new Animations
    architecture      = new Architecture
    bank              = new Bank
    battles           = new Battles
    blackboard        = new Blackboard
    productionHistory = new ProductionHistory
    bullets           = new Bullets
    camera            = new Camera
    coordinator       = new Coordinator
    accounting        = new Accounting
    fingerprints      = new Fingerprints
    groundskeeper     = new Groundskeeper
    gathering         = new Gathering
    geography         = new Geography
    grids             = new Grids
    history           = new History
    lambdas           = new LambdaQueue
    latency           = new Latency
    macroCounts       = new MacroCounts
    macroSim          = new MacroSim
    manners           = new Manners
    matchups          = new MatchupGraph
    paths             = new Paths
    placement         = new PlacementCycle
    preplacement      = new Preplacement
    priorities       = new Priorities
    projections       = new Projections
    reaction          = new ReactionTimes
    recruiter         = new Recruiter
    scheduler         = new Scheduler
    scouting          = new Scouting
    simulation        = new Simulation
    strategy          = new Strategist
    storyteller       = new Storyteller
    squads            = new Squads
    tactics           = new Tactician
    units             = new UnitTracker
    unitsShown        = new UnitsShown
    viewport          = new Viewport
    visualization     = new Visualization
    yolo              = new Yolo

    // Order-dependent initialization:
    // TaskQueue comes last because it references other systems
    tasks             = new TaskQueueGlobal
  }
  
  private def analyzeTerrain() {
    With.logger.debug(f"Loading fake BWTA for ${game.mapName} at ${game.mapFileName()}")
    try {
      BWTA.readMap(game)
      BWTA.analyze()
    } catch { case exception: Exception =>
      With.logger.quietlyOnException(exception)
      With.logger.debug("Retrying terrain analysis with assertions disabled.")
      // With the error logged, try again
      BWTA.readMap(game)
      BWTA.setFailOnError(false)
      BWTA.analyze()
    }
  }
}
