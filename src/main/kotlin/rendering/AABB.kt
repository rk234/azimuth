package rendering

import org.joml.Vector2f

data class AABB(
    var topLeft: Vector2f,
    var bottomRight: Vector2f,
) {
    fun intersects(aabb: AABB): Boolean {
        // Check if one AABB is to the left of the other
        if (bottomRight.x < aabb.topLeft.x || aabb.bottomRight.x < topLeft.x) {
            return false
        }
        // Check if one AABB is above the other
        if (bottomRight.y > aabb.topLeft.y || aabb.bottomRight.y > topLeft.y) {
            return false
        }
        return true
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