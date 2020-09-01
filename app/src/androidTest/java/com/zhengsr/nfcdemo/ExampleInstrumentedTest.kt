package com.zhengsr.nfcdemo

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private  val TAG = "ExampleInstrumentedTest"

    /**
     *
    @Test：把一个方法标记为测试方法
    @Before：每一个测试方法执行前自动调用一次
    @After：每一个测试方法执行完自动调用一次
    @BeforeClass：所有测试方法执行前执行一次，在测试类还没有实例化就已经被加载，所以用static修饰
    @AfterClass：所有测试方法执行完执行一次，在测试类还没有实例化就已经被加载，所以用static修饰
    @Ignore：暂不执行该测试方法

     */


    @Before
    fun beforeTest(){
        Log.d(TAG, "zsr beforeTest: ")
    }
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.zhengsr.nfcdemo", appContext.packageName)
        Log.d(TAG, "zsr useAppContext: ")


    }
}