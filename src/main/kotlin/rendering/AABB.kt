package rendering

import org.joml.Vector2f

data class AABB(
    var topLeft: Vector2f,
    var bottomRight: Vector2f,
) {
    fun intersects(aabb: AABB): Boolean {
//        if(aabb.topLeft.x > topLeft.x) {
//
//        }
        return false
    }

    fun contains(point: Vector2f): Boolean {
        return point.x >= topLeft.x &&
                point.x <= bottomRight.x &&
                point.y <= topLeft.y &&
                point.y >= bottomRight.y
    }

    fun growToInclude(point: Vector2f) {
        if(point.x < topLeft.x) {
            topLeft.x = point.x
        }
        if(point.x > bottomRight.x) {
            bottomRight.x = point.x
        }

        if(point.y < bottomRight.y) {
            bottomRight.y = point.y
        }
        if(point.y > topLeft.y) {
            topLeft.y = point.y
        }
    }
}