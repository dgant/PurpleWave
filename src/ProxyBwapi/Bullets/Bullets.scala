package ProxyBwapi.Bullets

import Lifecycle.With
import Performance.Cache

import scala.collection.JavaConverters._

class Bullets {
  def all: Seq[BulletInfo] = cachedBullets()
  
  private val cachedBullets = new Cache(() => With.game.getBullets.asScala.map(new BulletInfo(_)))
}
