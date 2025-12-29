package net.masuqat.intellij_partial_font_switcher

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testProjectService() {
        // プロジェクトが正しく初期化されているか確認
        assertNotNull(project)
    }

    fun testOpenFile() {
        // ファイルを作成してエディタで開くシミュレーション
        val psiFile = myFixture.configureByText("test.kt", "fun main() {}")
        assertEquals("test.kt", psiFile.name)
        assertNotNull(myFixture.editor)
    }

    /*
    // src/test/testData/MyTestData.kt が存在する場合のテスト例
    fun testWithTestData() {
        myFixture.configureByFile("MyTestData.kt")
        // 何らかの検証
    }
    */

    override fun getTestDataPath(): String {
        return "src/test/testData"
    }
}
