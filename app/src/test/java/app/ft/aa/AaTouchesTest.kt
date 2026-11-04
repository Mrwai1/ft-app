package app.ft.aa

import app.ft.aa.AaTouches.Companion.DOWN
import app.ft.aa.AaTouches.Companion.MOVE
import app.ft.aa.AaTouches.Companion.POINTER_DOWN
import app.ft.aa.AaTouches.Companion.POINTER_UP
import app.ft.aa.AaTouches.Companion.UP
import app.ft.aa.AaTouches.Event
import app.ft.aa.AaTouches.Pointer
import app.ft.core.ProtoReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AaTouchesTest {
    @Test fun oneFingerTapAndDrag() {
        val t = AaTouches()
        assertEquals(listOf(Event(DOWN, 0, listOf(Pointer(10, 20, 0)))), t.translate(DOWN, 10, 20, -1, -1, 0))
        assertEquals(listOf(Event(MOVE, 0, listOf(Pointer(15, 25, 0)))), t.translate(MOVE, 15, 25, -1, -1, 0))
        assertEquals(listOf(Event(UP, 0, listOf(Pointer(18, 28, 0)))), t.translate(UP, 18, 28, -1, -1, 0))
    }

    @Test fun pinchSendsBothFingersToAndroidAuto() {
        val t = AaTouches()
        t.translate(DOWN, 100, 100, -1, -1, 0)
        assertEquals(
            listOf(Event(POINTER_DOWN, 1, listOf(Pointer(100, 100, 0), Pointer(300, 300, 1)))),
            t.translate(POINTER_DOWN, 100, 100, 300, 300, 1)
        )
        assertEquals(
            listOf(Event(MOVE, 0, listOf(Pointer(80, 80, 0), Pointer(320, 320, 1)))),
            t.translate(MOVE, 80, 80, 320, 320, 0)
        )
        assertEquals(
            listOf(Event(POINTER_UP, 1, listOf(Pointer(80, 80, 0), Pointer(320, 320, 1)))),
            t.translate(POINTER_UP, 80, 80, 320, 320, 1)
        )
        assertEquals(listOf(Event(UP, 0, listOf(Pointer(80, 80, 0)))), t.translate(UP, 80, 80, -1, -1, 0))
    }

    @Test fun legacyPointerDownKeepsTheFirstFingerWhereItWas() {
        val t = AaTouches()
        t.translate(DOWN, 100, 100, -1, -1, 0)
        val down = t.translate(POINTER_DOWN, 400, 200, 400, 200, 1).single()
        assertEquals(listOf(Pointer(100, 100, 0), Pointer(400, 200, 1)), down.pointers)
    }

    @Test fun firstFingerLiftingLeavesTheSecondWithItsOwnId() {
        val t = AaTouches()
        t.translate(DOWN, 100, 100, -1, -1, 0)
        t.translate(POINTER_DOWN, 100, 100, 300, 300, 1)
        val lift = t.translate(POINTER_UP, 100, 100, 300, 300, 0).single()
        assertEquals(0, lift.index)
        assertEquals(listOf(Event(MOVE, 0, listOf(Pointer(310, 310, 1)))), t.translate(MOVE, 310, 310, -1, -1, 0))
        assertEquals(listOf(Event(UP, 0, listOf(Pointer(310, 310, 1)))), t.translate(UP, 310, 310, -1, -1, 0))
    }

    @Test fun liftingBothAtOnceEndsWithPointerUpThenUp() {
        val t = AaTouches()
        t.translate(DOWN, 100, 100, -1, -1, 0)
        t.translate(POINTER_DOWN, 100, 100, 300, 300, 1)
        val out = t.translate(UP, 90, 90, 310, 310, 0)
        assertEquals(listOf(POINTER_UP, UP), out.map { it.action })
        assertEquals(listOf(Pointer(90, 90, 0)), out[1].pointers)
    }

    @Test fun aThirdFingerIsIgnored() {
        val t = AaTouches()
        t.translate(DOWN, 1, 1, -1, -1, 0)
        t.translate(POINTER_DOWN, 1, 1, 2, 2, 1)
        assertTrue(t.translate(POINTER_DOWN, 1, 1, 3, 3, 2).isEmpty())
    }

    @Test fun wireMessageCarriesEveryPointer() {
        val event = Event(POINTER_DOWN, 1, listOf(Pointer(100, 100, 0), Pointer(300, 200, 1)))
        val touch = ProtoReader(AaMessages.inputTouches(42L, event)).message(3)!!
        val pointers = touch.messages(1)
        assertEquals(2, pointers.size)
        assertEquals(300, pointers[1].int(1))
        assertEquals(200, pointers[1].int(2))
        assertEquals(1, pointers[1].int(3))
        assertEquals(1, touch.int(2))
        assertEquals(POINTER_DOWN, touch.int(3))
    }
}
