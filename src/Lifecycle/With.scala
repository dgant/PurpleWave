package Lifecycle

import Debugging.Visualizations.{Viewport, Visualization}
import Debugging.{Camera, Configuration, Logger}
import Information.Battles.BattleClassifier
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Grids.Grids
import Information.Intelligenze.Fingerprinting.Fingerprints
import Information._
import Macro.Allocation._
import Macro.Architecture.{Architecture, PlacementScheduler}
import Macro.Scheduling.{MasterBuildOrderHistory, MasterBuildPlans, Projections, Scheduler}
import Micro.Agency.{Agency, Commander}
import Micro.Coordination.Coordinator
import Micro.Matchups.MatchupGraph
import Micro.Squads.Squads
import Performance.TaskQueue.{AbstractTaskQueue, TaskQueueGlobal}
import Planning.{Blackboard, Yolo}
import ProxyBwapi.Bullets.Bullets
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.ProxyBWMirror
import ProxyBwapi.UnitTracking.UnitTracker
import Strategery.History.History
import Strategery.{StarCraftMapMatcher, Strategist}
import _root_.Performance.{Latency, ReactionTimes, PerformanceMonitor}
import bwta.BWTA

import scala.collection.JavaConverters._

object With {
  var game              : bwapi.Game              = _
  var agents            : Agency                  = _
  var architecture      : Architecture            = _
  var bank              : Bank                    = _
  var blackboard        : Blackboard              = _
  var battles           : BattleClassifier        = _
  var buildOrderHistory : MasterBuildOrderHistory = _
  var buildPlans        : MasterBuildPlans        = _
  var bullets           : Bullets                 = _
  var bwapiData         : BwapiData               = _
  var camera            : Camera                  = _
  var commander         : Commander               = _
  var coordinator       : Coordinator             = _
  var configuration     : Configuration           = _
  var economy           : Economy                 = _
  var fingerprints      : Fingerprints            = _
  var geography         : Geography               = _
  var grids             : Grids                   = _
  var groundskeeper     : Groundskeeper           = _
  var history           : History                 = _
  var intelligence      : Intelligence            = _
  var latency           : Latency                 = _
  var logger            : Logger                  = _
  var matchups          : MatchupGraph            = _
  var paths             : Paths                   = _
  var performance       : PerformanceMonitor      = _
  var placement         : PlacementScheduler      = _
  var projections       : Projections             = _
  var proxy             : ProxyBWMirror           = _
  var prioritizer       : Prioritizer             = _
  var reaction          : ReactionTimes           = _
  var recruiter         : Recruiter               = _
  var scheduler         : Scheduler               = _
  var strategy          : Strategist              = _
  var squads            : Squads                  = _
  var tasks             : AbstractTaskQueue       = _
  var units             : UnitTracker             = _
  var viewport          : Viewport                = _
  var visualization     : Visualization           = _
  var yolo              : Yolo                    = _
  
  var self    : PlayerInfo         = _
  var neutral : PlayerInfo         = _
  var enemies : Vector[PlayerInfo] = _
  def enemy   : PlayerInfo        = enemies.head
  
  var frame           : Int     = 0
  var mapTileWidth    : Int     = 0
  var mapTileHeight   : Int     = 0
  var mapFileName     : String  = _
  var mapId           : String  = _
  def mapPixelWidth   : Int     = mapTileWidth * 32
  def mapPixelHeight  : Int     = mapTileHeight * 32
  def mapWalkWidth    : Int     = mapTileWidth * 4
  def mapWalkHeight   : Int     = mapTileHeight * 4
  
  def framesSince(previousFrame: Int): Int = Math.max(0, frame - previousFrame)
  
  def onFrame() {
    frame = With.game.getFrameCount
  }
  
  def onStart() {
    game.setLatCom(false)
    game.enableFlag(1) //Enable unit control
    game.setLocalSpeed(0)
    
    proxy             = new ProxyBWMirror
    self              = Players.get(game.self)
    neutral           = Players.get(game.neutral)
    enemies           = game.enemies.asScala.map(Players.get).toVector
    mapTileWidth      = game.mapWidth
    mapTileHeight     = game.mapHeight
    mapFileName       = game.mapFileName
    mapId             = StarCraftMapMatcher.clean(mapFileName)
    configuration     = new Configuration
    logger            = new Logger
    initializeBWTA()
    agents            = new Agency
    architecture      = new Architecture
    bank              = new Bank
    battles           = new BattleClassifier
    blackboard        = new Blackboard
    buildOrderHistory = new MasterBuildOrderHistory
    buildPlans        = new MasterBuildPlans
    bullets           = new Bullets
    bwapiData         = new BwapiData
    camera            = new Camera
    commander         = new Commander
    coordinator       = new Coordinator
    economy           = new Economy
    fingerprints      = new Fingerprints
    groundskeeper     = new Groundskeeper
    geography         = new Geography
    grids             = new Grids
    history           = new History
    intelligence      = new Intelligence
    latency           = new Latency
    matchups          = new MatchupGraph
    paths             = new Paths
    performance       = new PerformanceMonitor
    placement         = new PlacementScheduler
    prioritizer       = new Prioritizer
    projections       = new Projections
    reaction          = new ReactionTimes
    recruiter         = new Recruiter
    scheduler         = new Scheduler
    strategy          = new Strategist
    squads            = new Squads
    tasks             = new TaskQueueGlobal
    units             = new UnitTracker
    viewport          = new Viewport
    visualization     = new Visualization
    yolo              = new Yolo
    
    game.setLocalSpeed(0)
  }
  
  def onEnd() {
    With.logger.flush()
    BWTA.cleanMemory()
  }
  
  private def initializeBWTA() {
    With.logger.debug("Loading BWTA for " + With.game.mapName + " at " + With.game.mapFileName())
    BWTA.readMap()
    BWTA.analyze()
    //These may not be necessary or helpful since BWTA2 doesn't seem to work in BWMirror
    BWTA.computeDistanceTransform()
    BWTA.buildChokeNodes()
  }
}
