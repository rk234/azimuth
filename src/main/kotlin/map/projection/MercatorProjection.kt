package map.projection

import org.joml.Vector2f

class MercatorProjection : MapProjection {
    //https://en.wikipedia.org/wiki/Mercator_projection
    override fun toCartesian(latitude: Float, longitude: Float): Vector2f {
        TODO("Not yet implemented")
    }

    override fun fromCartesian(point: Vector2f): Pair<Float, Float> {
        TODO("Not yet implemented")
    }
}