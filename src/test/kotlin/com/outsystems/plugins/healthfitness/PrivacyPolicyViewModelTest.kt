import android.net.http.HttpException
import androidx.lifecycle.SavedStateHandle
import com.outsystems.plugins.healthfitness.utils.HttpConnector
import com.outsystems.plugins.healthfitness.viewmodel.PrivacyPolicyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.Thread.sleep

@ExperimentalCoroutinesApi
class PrivacyPolicyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PrivacyPolicyViewModel
    private val savedStateHandle: SavedStateHandle = mock()
    private val httpConnector: HttpConnector = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PrivacyPolicyViewModel(savedStateHandle, httpConnector)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchContent with valid URL updates content`() = runBlocking {
        val testUrl = "http://test.com"
        val testContent = "Test Content"
        doAnswer { testContent }.whenever(httpConnector).connectionURL(any())
        whenever(savedStateHandle.get<String>(PrivacyPolicyViewModel.CONTENT_KEY)).thenReturn(null)

        viewModel.fetchContent(testUrl, "default", "error")

        sleep(1000)
        assertEquals(testContent, viewModel.content.value)
    }

    @Test
    fun `fetchContent with blank URL returns default content with no prev state`() = runTest {
        val defaultContent = "Default Content"
        whenever(savedStateHandle.get<String>(PrivacyPolicyViewModel.CONTENT_KEY)).thenReturn(null)
        viewModel.fetchContent("", defaultContent, "error")

        assertEquals(defaultContent, viewModel.content.value)
    }

    @Test
    fun `fetchContent with blank URL returns default content with prev state`() = runTest {
        val defaultContent = "Default Content"
        val testContent = "Test Content"
        whenever(savedStateHandle.get<String>(PrivacyPolicyViewModel.CONTENT_KEY)).thenReturn(testContent)
        viewModel.fetchContent("", defaultContent, "error")

        assertEquals(testContent, viewModel.content.value)
    }

    @Test
    fun `fetchContent with error returns error content`() = runBlocking {
        val errorContent = "Error Content"
        whenever(savedStateHandle.get<String>(PrivacyPolicyViewModel.CONTENT_KEY)).thenReturn(null)
        doAnswer { HttpException("URL not valid", null) }.whenever(httpConnector).connectionURL(any())

        viewModel.fetchContent("http://error.com", "default", errorContent)
        sleep(1000)
        assertEquals(errorContent, viewModel.content.value)
    }
}
