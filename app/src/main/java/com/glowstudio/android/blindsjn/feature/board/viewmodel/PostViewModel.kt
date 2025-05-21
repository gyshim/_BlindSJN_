package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.board.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _reportResult = MutableStateFlow<String?>(null)
    val reportResult: StateFlow<String?> = _reportResult

    fun setStatusMessage(message: String) {
        _statusMessage.value = message
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadPosts()
                if (response.isSuccessful) {
                    response.body()?.let { postListResponse ->
                        _posts.value = postListResponse.data
                    }
                } else {
                    _statusMessage.value = "불러오기 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러: ${e.message}"
            }
        }
    }

    fun loadPostById(postId: Int) {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadPostById(postId)
                if (response.isSuccessful) {
                    response.body()?.let { postDetailResponse ->
                        _selectedPost.value = postDetailResponse.data
                    }
                } else {
                    _statusMessage.value = "게시글 조회 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "게시글 조회 에러: ${e.message}"
            }
        }
    }

    fun savePost(title: String, content: String, userId: Int, industry: String) {
        viewModelScope.launch {
            try {
                val postRequest = PostRequest(title, content, userId, industry)
                val response = PostRepository.savePost(postRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    loadPosts()
                } else {
                    _statusMessage.value = "저장 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun editPost(postId: Int, title: String, content: String) {
        viewModelScope.launch {
            try {
                val editRequest = EditPostRequest(postId, title, content)
                val response = PostRepository.editPost(editRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    loadPostById(postId)
                } else {
                    _statusMessage.value = "수정 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                val deleteRequest = DeleteRequest(postId)
                val response = PostRepository.deletePost(deleteRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    loadPosts()
                } else {
                    _statusMessage.value = "삭제 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun incrementLike(postId: Int) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(likeCount = post.likeCount + 1)
            } else {
                post
            }
        }
    }

    fun decrementLike(postId: Int) {
        // TODO: 서버에 좋아요 감소 요청 또는 로컬에서 처리
        // 예시: PostRepository.decrementLike(postId)
    }

    fun toggleLike(postId: Int, userId: Int, onResult: (Boolean, Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val request = LikePostRequest(post_id = postId, user_id = userId)
                val response = PostRepository.likePost(request)
                if (response.isSuccessful) {
                    // 서버에서 최신 게시글 정보 다시 불러오기
                    val updatedPost = PostRepository.loadPostById(postId).body()?.data
                    loadPostById(postId)
                    loadPosts()
                    onResult(true, updatedPost?.isLiked ?: false, updatedPost?.likeCount ?: 0)
                } else {
                    onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
                }
            } catch (e: Exception) {
                onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
            }
        }
    }

    fun reportPost(postId: Int, userId: Int, reason: String) {
        viewModelScope.launch {
            try {
                val request = ReportRequest(postId, userId, reason)
                val response = PostRepository.reportPost(request)
                if (response.isSuccessful) {
                    response.body()?.let { reportResponse ->
                        if (reportResponse.success) {
                            _reportResult.value = reportResponse.message ?: "신고가 접수되었습니다."
                        } else {
                            _reportResult.value = reportResponse.error ?: "신고 접수 실패"
                        }
                    } ?: run {
                        _reportResult.value = "서버 응답이 비어있습니다."
                    }
                } else {
                    _reportResult.value = "서버 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                _reportResult.value = "신고 중 오류 발생: ${e.message}"
            }
        }
    }

    fun clearReportResult() {
        _reportResult.value = null
    }
} 