package Micro.LearnedCombat

import scala.collection.mutable

/** Exact 512-pixel neighborhood counts over one game-thread position snapshot. */
object LearnedCombatNeighbors {
  final case class Point(x: Int, y: Int, player: Int)
  private val Radius = 512
  private val RadiusSquared = Radius.toLong * Radius
  private val CellSize = 128
  private def key(x: Int, y: Int): Long = (x.toLong << 32) ^ (y.toLong & 0xffffffffL)

  private final class Bucket(val points: Array[Point]) {
    val minX: Int = points.map(_.x).min
    val maxX: Int = points.map(_.x).max
    val minY: Int = points.map(_.y).min
    val maxY: Int = points.map(_.y).max
    val players: Map[Int, Int] = points.groupBy(_.player).map { case (player, units) => player -> units.length }
  }

  def counts(points: IndexedSeq[Point]): Array[(Int, Int)] = {
    val positions = points.toArray
    // For small observations, primitive snapshots avoid index setup and lookup.
    if (positions.length <= 256) return positions.map(p => countPoints(p, positions))
    val groups = new mutable.LongMap[mutable.ArrayBuffer[Point]]
    positions.foreach(p => groups.getOrElseUpdate(key(Math.floorDiv(p.x, CellSize), Math.floorDiv(p.y, CellSize)),
      new mutable.ArrayBuffer[Point]) += p)
    val buckets = new mutable.LongMap[Bucket]
    groups.foreach { case (cell, group) => buckets.put(cell, new Bucket(group.toArray)) }
    positions.map(p => {
      var allies = 0
      var enemies = 0
      var cy = Math.floorDiv(p.y - Radius, CellSize)
      val lastY = Math.floorDiv(p.y + Radius, CellSize)
      val firstX = Math.floorDiv(p.x - Radius, CellSize)
      val lastX = Math.floorDiv(p.x + Radius, CellSize)
      while (cy <= lastY) {
        var cx = firstX
        while (cx <= lastX) {
          val bucket = buckets.getOrElse(key(cx, cy), null)
          if (bucket != null) {
            val nearX = Math.max(0L, Math.max(bucket.minX.toLong - p.x, p.x.toLong - bucket.maxX))
            val nearY = Math.max(0L, Math.max(bucket.minY.toLong - p.y, p.y.toLong - bucket.maxY))
            if (nearX * nearX + nearY * nearY <= RadiusSquared) {
              val farX = Math.max(Math.abs(bucket.minX.toLong - p.x), Math.abs(bucket.maxX.toLong - p.x))
              val farY = Math.max(Math.abs(bucket.minY.toLong - p.y), Math.abs(bucket.maxY.toLong - p.y))
              if (farX * farX + farY * farY <= RadiusSquared) {
                val own = bucket.players.getOrElse(p.player, 0)
                allies += own
                enemies += bucket.points.length - own
              } else {
                val partial = countPoints(p, bucket.points)
                allies += partial._1
                enemies += partial._2
              }
            }
          }
          cx += 1
        }
        cy += 1
      }
      (allies, enemies)
    })
  }

  private def countPoints(p: Point, points: Array[Point]): (Int, Int) = {
    var allies = 0
    var enemies = 0
    var i = 0
    while (i < points.length) {
      val other = points(i)
      val dx = other.x.toLong - p.x
      val dy = other.y.toLong - p.y
      if (dx * dx + dy * dy <= RadiusSquared) {
        if (other.player == p.player) allies += 1 else enemies += 1
      }
      i += 1
    }
    (allies, enemies)
  }
}
