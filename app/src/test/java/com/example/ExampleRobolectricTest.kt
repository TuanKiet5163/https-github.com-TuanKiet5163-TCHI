package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.VndFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TCHI", appName)
    }

    @Test
    fun `test VND currency formatting`() {
        assertEquals("150.000 VNĐ", VndFormatter.formatVnd(150000L))
        assertEquals("1.500.000 VNĐ", VndFormatter.formatVnd(1500000L))
        assertEquals("0 VNĐ", VndFormatter.formatVnd(0L))
        assertEquals("50.000 VNĐ", VndFormatter.formatVnd(50000L))
    }

    @Test
    fun `test VND input parser`() {
        assertEquals("150.000", VndFormatter.formatInput("150000"))
        assertEquals("1.500.000", VndFormatter.formatInput("1500000"))
        assertEquals(150000L, VndFormatter.parseToLong("150.000 VNĐ"))
    }
}
