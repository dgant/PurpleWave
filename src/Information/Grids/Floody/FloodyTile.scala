package Information.Grids.Floody

import Mathematics.Points.Tile
import Performance.{Cache, CacheForever}
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable.ArrayBuffer

final class FloodyTile(val i: Int, margin: Int) {
  val tile      = new Tile(i)
  val depths    = new UnorderedBuffer[FloodyUnitDepth](12)
  var maxDepth  = 0

  def invalidateCaches(): Unit = {
    caches.foreach(_.invalidate())
  }

  @inline def dpfGround     : Double = _dpfGround()
  @inline def dpfAir        : Double = _dpfAir()
  @inline def damageGround  : Double = _damageGround()
  @inline def damageAir     : Double = _damageGround()

  private val caches = new ArrayBuffer[Cache[Double]]
  private def addCache(cache: Cache[Double]): Cache[Double] = {
    caches += cache
    cache
  }

  private val _dpfGround    = addCache(new CacheForever(() => depths.view.filter(_.value > margin).map(_.unit.unit.dpfGround        ).sum))
  private val _dpfAir       = addCache(new CacheForever(() => depths.view.filter(_.value > margin).map(_.unit.unit.dpfAir           ).sum))
  private val _damageGround = addCache(new CacheForever(() => depths.view.filter(_.value > margin).map(_.unit.unit.damageOnHitGround).sum))
  private val _damageAir    = addCache(new CacheForever(() => depths.view.filter(_.value > margin).map(_.unit.unit.damageOnHitAir   ).sum))
}
