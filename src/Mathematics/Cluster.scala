package Mathematics

import Mathematics.Points.Pixel

import scala.collection.mutable

object Cluster {
  
  def apply[T](
    things        : Iterable[T],
    radius        : Double,
    limitRegion   : Boolean = false,
    extractPixel  : (T) => Pixel)
      : mutable.HashMap[T, mutable.HashSet[T]] = {
    
    val neighborsByUnit = mapToNeighbors(things, radius, extractPixel)
    val unitLeaders = new mutable.HashMap[T, T]
    val groupsByLeader = new mutable.HashMap[T, mutable.HashSet[T]] {
      override def default(key: T):mutable.HashSet[T] = {
        put(key, new mutable.HashSet[T])
        this(key)}}
    
    things.foreach(thing => {
      if ( ! unitLeaders.contains(thing)) {
        groupsByLeader(thing).add(thing)
        groupsByLeader(thing) ++= neighborsByUnit(thing).filter(neighbor =>
          ! limitRegion ||
          (
            extractPixel(thing).tile.altitude ==
            extractPixel(neighbor).tile.altitude &&
            extractPixel(thing).zone ==
            extractPixel(neighbor).zone
          ))
        groupsByLeader(thing).foreach(groupMember => unitLeaders.put(groupMember, thing))
      }})
    
    groupsByLeader
  }
  
  private def mapToNeighbors[T](
    things        : Iterable[T],
    radius        : Double,
    extractPixel  : (T) => Pixel)
      :Map[T, Iterable[T]] = {
    
    val radiusSquared = radius * radius
    
    // Yes, this includes the unit itself
    things.map(thing => (
        thing,
        things
          .filter(otherThing => extractPixel(otherThing).pixelDistanceSquared(extractPixel(thing)) <= radiusSquared)))
          .toMap
  }
}
