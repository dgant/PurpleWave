package Lifecycle

import Debugging.Visualizations.{Viewport, Visualization}
import Debugging.{Camera, Configuration, Logger}
import Information.Battles.BattleClassifier
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Grids.Grids
import Information._
import Macro.Allocation._
import Macro.Scheduling.Scheduler
import Macro.SimCity.Groundskeeper
import Micro.Intent.Commander
import Micro.Task.Executor
import Performance.TaskQueue.{AbstractTaskQueue, TaskQueueGlobal}
import Planning.Plans.GamePlans.WinTheGame
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.ProxyBWMirror
import ProxyBwapi.UnitTracking.UnitTracker
import _root_.Performance.{Latency, PerformanceMonitor}
import bwta.BWTA

import scala.collection.JavaConverters._

object With {
  var game            : bwapi.Game          = _
  var bank            : Bank                = _
  var groundskeeper   : Groundskeeper       = _
  var camera          : Camera              = _
  var configuration   : Configuration       = _
  var battles         : BattleClassifier    = _
  var executor        : Executor            = _
  var commander       : Commander           = _
  var economy         : Economy             = _
  var grids           : Grids               = _
  var intelligence    : Intelligence        = _
  var geography       : Geography           = _
  var gameplan        : WinTheGame          = _
  var latency         : Latency             = _
  var logger          : Logger              = _
  var paths           : Paths               = _
  var performance     : PerformanceMonitor  = _
  var proxy           : ProxyBWMirror       = _
  var prioritizer     : Prioritizer         = _
  var realEstate      : RealEstate          = _
  var recruiter       : Recruiter           = _
  var scheduler       : Scheduler           = _
  var tasks           : AbstractTaskQueue   = _
  var units           : UnitTracker         = _
  var viewport        : Viewport            = _
  var visualization   : Visualization       = _
  
  var self    : PlayerInfo         = _
  var neutral : PlayerInfo         = _
  var enemies : Vector[PlayerInfo] = _
  
  var frame     : Int = 0
  var mapWidth  : Int = 0
  var mapHeight : Int = 0
  
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
    mapWidth          = game.mapWidth
    mapHeight         = game.mapHeight
    configuration     = new Configuration
    logger            = new Logger
    initializeBWTA()
    bank             = new Bank
    battles          = new BattleClassifier
    groundskeeper    = new Groundskeeper
    camera           = new Camera
    economy          = new Economy
    executor         = new Executor
    commander        = new Commander
    gameplan         = new WinTheGame
    geography        = new Geography
    grids            = new Grids
    intelligence     = new Intelligence
    latency          = new Latency
    paths            = new Paths
    performance      = new PerformanceMonitor
    prioritizer      = new Prioritizer
    realEstate       = new RealEstate
    recruiter        = new Recruiter
    scheduler        = new Scheduler
    tasks            = new TaskQueueGlobal
    units            = new UnitTracker
    viewport         = new Viewport
    visualization    = new Visualization
    
    game.setLocalSpeed(configuration.gameSpeed)
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
