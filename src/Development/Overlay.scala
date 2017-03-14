package Development

import Geometry.Grids.Abstract.GridConcrete
import Geometry.TileRectangle
import Global.Combat.Battle.Battle
import Plans.Allocation.{LockCurrency, LockUnits}
import Plans.Plan
import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.{Color, Player, Position, UnitCommandType}

import scala.collection.JavaConverters._

object Overlay {
  def onFrame() {
    if (With.configuration.enableOverlay) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
      if (With.configuration.enableOverlayBattles)        _drawBattles()
      if (With.configuration.enableOverlayEconomy)        _drawEconomy()
      if (With.configuration.enableOverlayFrameLength)    _drawFrameLength()
      if (With.configuration.enableOverlayGrids)          _drawGrids()
      if (With.configuration.enableOverlayUnits)          _drawUnits()
      if (With.configuration.enableOverlayPlans)          _drawPlans()
      if (With.configuration.enableOverlayResources)      _drawResources()
      if (With.configuration.enableOverlayScheduler)      _drawScheduler()
      if (With.configuration.enableOverlayTerrain)        _drawTerrain()
      if (With.configuration.enableOverlayTrackedUnits)   _drawTrackedUnits()
    }
  }
  
  def _drawBattles() = With.battles.all.foreach(_drawBattle)
  def _drawBattle(battle:Battle) {
    //if (battle.enemy.strength * battle.us.strength == 0) return
    //if (battle.us.vanguard.getDistance(battle.enemy.vanguard) > 32 * 20) return
    With.game.drawCircleMap(battle.focus, 8, Color.Brown)
    With.game.drawCircleMap(battle.us.vanguard, 8, Color.Blue)
    With.game.drawCircleMap(battle.enemy.vanguard, 8, Color.Red)
    With.game.drawLineMap(battle.focus, battle.us.vanguard, Color.Blue)
    With.game.drawLineMap(battle.focus, battle.enemy.vanguard, Color.Red)
    With.game.drawBoxMap(
      battle.us.units.map(_.position).minBound,
      battle.us.units.map(_.position).maxBound,
      Color.Blue)
    With.game.drawBoxMap(
      battle.enemy.units.map(_.position).minBound,
      battle.enemy.units.map(_.position).maxBound,
      Color.Red)
    _drawTextLabel(
      List(battle.us.strength/100 + " - " + battle.enemy.strength/100),
      battle.focus,
      drawBackground = true,
      backgroundColor = Color.Brown)
  }
  
  def _drawEconomy() {
    val labels = List(
      "Active miners:",
      "Active drillers:",
      "Minerals per minute:",
      "Gas per minute:",
      "Total minerals (est.):",
      "Total gas (est.):",
      "Total minerals (real):",
      "Total gas (real):"
    )
    val values = List(
      With.economy.ourActiveMiners.size,
      With.economy.ourActiveDrillers.size,
      With.economy.ourMineralIncomePerMinute,
      With.economy.ourGasIncomePerMinute,
      With.economy.ourEstimatedTotalMinerals.toInt,
      With.economy.ourEstimatedTotalGas.toInt,
      With.economy.ourActualTotalMinerals,
      With.economy.ourActualTotalGas
    )
    With.game.drawTextScreen(450, 5, labels.mkString("\n"))
    With.game.drawTextScreen(550, 5, values.mkString("\n"))
  }
  
  def _drawFrameLength() {
    With.game.drawTextScreen(300, 5, With.performance.meanFrameLength + " ms/f")
    With.game.drawTextScreen(350, 5, With.performance.frameDelay(1) + " base cache delay")
  }
  
  def _drawGrids() {
    _drawGrid(With.grids.enemyGroundStrength, 0, 0)
    _drawGrid(With.grids.friendlyGroundStrength, 0, 1)
  }
  
  def _drawGrid[T](map:GridConcrete[T], offsetX:Int=0, offsetY:Int=0) {
    map.positions
      .filter(tilePosition => map.get(tilePosition) != 0 &&  map.get(tilePosition) != false)
      .foreach(tilePosition => With.game.drawTextMap(tilePosition.toPosition.add(offsetX*16, offsetY*13), map.repr(map.get(tilePosition))))
  }
  
  def _drawPlans() {
    With.game.drawTextScreen(5, 5, _describePlanTree(With.gameplan, 0, 0))
    _drawPlansRecursively(With.gameplan)
  }
  
  def _drawPlansRecursively(plan:Plan) {
    plan.drawOverlay()
    plan.getChildren.foreach(_drawPlansRecursively)
  }
  
  def _drawUnits() {
    With.units.ours
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
      With.game.drawCircleMap(unit.position, 32, Color.Orange))
    With.units.ours
      .filterNot(_.command.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(
        List(
          With.commander._lastIntentions.get(unit).map(intent => (intent.motivation * 100).toInt.toString).getOrElse(""),
          With.commander._lastIntentions.get(unit).map(intent => intent.plan.toString).getOrElse(""),
          With.commander._lastCommands.get(unit).getOrElse(""),
          unit.command.getUnitCommandType.toString),
        unit.position,
        drawBackground = false))
  }
  
  def _drawTerrain() {
    With.geography.zones.foreach(zone => {
      _drawPolygonPositions(zone.region.getPolygon.getPoints.asScala)
      With.game.drawLineMap(
        zone.region.getPolygon.getPoints.asScala.head,
        zone.region.getPolygon.getPoints.asScala.last,
        bwapi.Color.Brown)
      _drawTextLabel(
        List(zone.region.getCenter.toString, zone.region.getCenter.toTilePosition.toString, zone.owner.getName),
        zone.region.getCenter)
      
      zone.edges.foreach(edge => {
        _drawTextLabel(
          List(edge.zones.map(_.centroid.toString).mkString(" -> ")),
          edge.chokepoint.getCenter)
        With.game.drawCircleMap(edge.chokepoint.getCenter, edge.chokepoint.getWidth.toInt/2, Color.Purple)
        With.game.drawLineMap(edge.chokepoint.getSides.first, edge.chokepoint.getSides.second, Color.Purple)
      })
      
      zone.bases.foreach(base => {
        _drawTileRectangle(base.harvestingArea, Color.Cyan)
        _drawTileRectangle(base.townHallArea, Color.Yellow)
        _drawTextLabel(
          List(base.zone.owner.getName, if (base.isStartLocation) "Start location" else ""),
          base.townHallArea.startInclusive.topLeftPixel,
          true,
          _getPlayerColor(base.zone.owner))
      })
    })
  }
  
  def _drawResources() {
    With.game.drawTextScreen(
      305,
      5,
      With.bank.getPrioritizedRequests
        .take(8)
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
          (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
          (if (r.gas      > 0)  r.gas       .toString ++ "g " else "") ++
          (if (r.supply   > 0)  r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
  
  def _drawScheduler() {
    
    With.game.drawTextScreen(0, 85, "Build queue")
    _drawTable(0, 100, With.scheduler.queue.take(20).map(buildable => List(buildable.toString)))
    With.game.drawTextScreen(150, 85, "Next to build")
    _drawTable(150, 100, With.scheduler.simulationResults.suggestedEvents
      .toList
      .sortBy(_.buildable.toString)
      .sortBy(_.frameEnd)
      .sortBy(_.frameStart)
      .take(20)
      .map(event => List(event.toString.split("\\s+")(0), _reframe(event.frameStart), _reframe(event.frameEnd))))
    With.game.drawTextScreen(300, 85, "Started")
    _drawTable(300, 100, With.scheduler.simulationResults.simulatedEvents
      .filter(e => e.frameStart < With.game.getFrameCount)
      .toList
      .sortBy(_.buildable.toString)
      .sortBy(_.frameStart)
      .sortBy(_.frameEnd)
      .take(20)
      .map(event => List(event.toString.split("\\s+")(0), _reframe(event.frameStart), _reframe(event.frameEnd))))
    With.game.drawTextScreen(500, 85, "Impossible")
    _drawTable(500, 100, With.scheduler.simulationResults.unbuildable
      .toList
      .take(20)
      .map(buildable => List(buildable.toString.split("\\s+")(0))))
  }
  
  def _reframe(frameAbsolute:Int):String = {
    val reframed = (frameAbsolute - With.game.getFrameCount)/24
    if (reframed <= 0) "Started" else reframed.toString
  }
  
  def _drawTable(startX:Int, startY:Int, cells:Iterable[Iterable[String]]) {
    cells.zipWithIndex.foreach(pair => _drawTableRow(startX, startY, pair._2, pair._1))
  }
  
  def _drawTableRow(startX:Int, startY:Int, rowIndex:Int, row:Iterable[String]) {
    row.zipWithIndex.foreach(pair => With.game.drawTextScreen(
      startX + pair._2 * 50,
      startY + rowIndex * 13,
      pair._1))
  }
  
  def _drawTrackedUnits() {
    With.units.enemy.foreach(_drawTrackedUnit)
    With.units.neutral.foreach(_drawTrackedUnit)
  }
  
  def _drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      With.game.drawCircleMap(
        trackedUnit.position,
        trackedUnit.utype.width / 2,
        _getPlayerColor(trackedUnit.player))
      _drawTextLabel(
        List(TypeDescriber.unit(trackedUnit.utype)),
        trackedUnit.position,
        drawBackground = true,
        _getPlayerColor(trackedUnit.player))
    }
  }
  
  def _describePlanTree(plan:Plan, childOrder:Integer, depth:Integer):String = {
    if (_isRelevant(plan)) {
      (_describePlan(plan, childOrder, depth)
        ++ plan.getChildren.zipWithIndex.map(x => _describePlanTree(x._1, x._2, depth + 1)))
        .mkString("")
    } else ""
  }
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val checkbox = if (plan.isComplete) "X " else "  "
    val spacer = "  " * depth
    val leftColumn =
      (checkbox
        ++ spacer
        ++ "#"
        ++ (childOrder + 1).toString
        ++ " "
        ++ plan.toString)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    plan.getChildren.exists(child => _isRelevant(child) || ((child.isInstanceOf[LockCurrency] || child.isInstanceOf[LockUnits]) && child.isComplete))
  }
  
  def _drawTextLabel(
    textLines:Iterable[String],
    position:Position,
    drawBackground:Boolean = false,
    backgroundColor:Color = Color.Grey) {
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.size).max) / 2
    val boxWidth = estimatedTextWidth + 2 * horizontalMargin
    val boxHeight = 11 * textLines.size
    val textX = position.getX - boxWidth/2
    val textY = position.getY - boxHeight/2
    val boxX = textX - horizontalMargin
    val boxY = textY
    
    if (drawBackground) {
      With.game.drawBoxMap(
        boxX,
        boxY,
        boxX + boxWidth,
        boxY + boxHeight,
        backgroundColor,
        true) //isSolid
    }
    With.game.drawTextMap(
      textX,
      textY,
      textLines.mkString("\n"))
  }
  
  def _drawTileRectangle(rectangle:TileRectangle, color:Color) {
    With.game.drawBoxMap(rectangle.startPosition, rectangle.endPosition, color)
  }
  
  def _drawPolygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
  
  def _getPlayerColor(player:Player):Color = {
    if (player.isNeutral) Color.Grey
    else if (player.isEnemy(With.game.self)) Color.Red
    else Color.Blue
  }
}
