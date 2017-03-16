package Startup

import Debugging.{AutoCamera, Configuration, Logger, Performance}
import Macro.Allocation._
import Micro.Battles.Battles
import ProxyBwapi.UnitTracking.UnitTracker
import Information.Grids.Grids
import Information.Geography.Geography
import Information._
import Macro.Scheduling.Scheduler
import Planning.Plans.GamePlans.WinTheGame
import Macro.Architect
import Micro.{Commander, Paths}
import _root_.Performance.Latency
import bwapi.Player

import scala.collection.mutable

object With {
  var game:bwapi.Game = null
  var architect:Architect = null
  var bank:Banker = null
  var camera:AutoCamera = null
  var configuration:Configuration = null
  var battles:Battles = null
  var commander:Commander = null
  var economy:Economy = null
  var grids:Grids = null
  var intelligence:Intelligence = null
  var geography:Geography = null
  var gameplan:WinTheGame = null
  var latency:Latency = null
  var logger:Logger = null
  var paths:Paths = null
  var performance:Performance = null
  var prioritizer:Prioritizer = null
  var recruiter:Recruiter = null
  var scheduler:Scheduler = null
  var units: UnitTracker = null
  
  var self:Player = null
  var frame = 0
  var mapWidth = 0
  var mapHeight = 0
}
