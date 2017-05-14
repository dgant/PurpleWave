package Lifecycle

import Debugging.Visualizations.Viewport
import Debugging.{AutoCamera, Configuration, Logger}
import Information.Battles.BattleClassifier
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Grids.Grids
import Information._
import Macro.Allocation._
import Macro.Architect
import Macro.Scheduling.Scheduler
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
  var game          : bwapi.Game          = null
  var architect     : Architect           = null
  var bank          : Bank                = null
  var camera        : AutoCamera          = null
  var configuration : Configuration       = null
  var battles       : BattleClassifier    = null
  var executor      : Executor            = null
  var commander     : Commander           = null
  var economy       : Economy             = null
  var grids         : Grids               = null
  var intelligence  : Intelligence        = null
  var geography     : Geography           = null
  var gameplan      : WinTheGame          = null
  var latency       : Latency             = null
  var logger        : Logger              = null
  var paths         : Paths               = null
  var performance   : PerformanceMonitor  = null
  var proxy         : ProxyBWMirror       = null
  var prioritizer   : Prioritizer         = null
  var realEstate    : RealEstate          = null
  var recruiter     : Recruiter           = null
  var scheduler     : Scheduler           = null
  var tasks         : AbstractTaskQueue   = null
  var units         : UnitTracker         = null
  var viewport      : Viewport            = null
  
  var self    : PlayerInfo         = null
  var neutral : PlayerInfo         = null
  var enemies : Vector[PlayerInfo] = null
  
  var frame     : Int = 0
  var mapWidth  : Int = 0
  var mapHeight : Int = 0
  
  def onFrame() {
    frame = With.game.getFrameCount
  }
  
  def onStart() {
    With.game.setLatCom(false)
    With.game.enableFlag(1) //Enable unit control
    With.game.setLocalSpeed(0)
    
    With.proxy                  = new ProxyBWMirror
    With.self                   = Players.get(With.game.self)
    With.neutral                = Players.get(With.game.neutral)
    With.enemies                = With.game.enemies.asScala.map(Players.get).toVector
    With.mapWidth               = With.game.mapWidth
    With.mapHeight              = With.game.mapHeight
    With.configuration          = new Configuration
    With.logger                 = new Logger
    initializeBWTA()
    With.architect              = new Architect
    With.bank                   = new Bank
    With.battles                = new BattleClassifier
    With.camera                 = new AutoCamera
    With.economy                = new Economy
    With.executor               = new Executor
    With.commander              = new Commander
    With.gameplan               = new WinTheGame
    With.geography              = new Geography
    With.grids                  = new Grids
    With.intelligence           = new Intelligence
    With.latency                = new Latency
    With.paths                  = new Paths
    With.performance            = new PerformanceMonitor
    With.prioritizer            = new Prioritizer
    With.realEstate             = new RealEstate
    With.recruiter              = new Recruiter
    With.scheduler              = new Scheduler
    With.tasks                  = new TaskQueueGlobal
    With.units                  = new UnitTracker
    With.viewport               = new Viewport
    
    With.game.setLocalSpeed(With.configuration.gameSpeed)
  }
  
  def onEnd() {
    With.logger.flush
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
