// 게시글 상세 보기
async function loadPost() {
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');
    
    if (!postId) {
        alert('게시글 ID가 없습니다');
        window.location.href = 'board.html';
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        // 게시글 정보
        const postResponse = await fetch(`/api/board/posts/${postId}`, {
            headers: headers
        });
        
        if (!postResponse.ok) {
            const errorText = await postResponse.text();
            console.error('게시글 로드 실패:', postResponse.status, errorText);
            alert('게시글을 불러올 수 없습니다');
            window.location.href = 'board.html';
            return;
        }
        
        const postResult = await postResponse.json();
        
        if (!postResult.success) {
            alert(postResult.message || '게시글을 불러올 수 없습니다');
            window.location.href = 'board.html';
            return;
        }
        
        const post = postResult.data;
        
        if (!post) {
            alert('게시글을 찾을 수 없습니다');
            window.location.href = 'board.html';
            return;
        }
        
        // 댓글 정보
        const commentsResponse = await fetch(`/api/board/posts/${postId}/comments`, {
            headers: headers
        });
        
        let comments = [];
        if (commentsResponse.ok) {
            const commentsResult = await commentsResponse.json();
            comments = commentsResult.success ? commentsResult.data : [];
        }
        
        renderPost(post, comments);
        // renderPost가 완료된 후 아바타 업데이트
        setTimeout(() => {
            updateCurrentUserAvatar();
        }, 100);
    } catch (error) {
        console.error('게시글 로드 오류:', error);
        alert('게시글을 불러오는 중 오류가 발생했습니다: ' + error.message);
    }
}

// 전역 변수
let currentPost = null;
let currentComments = [];

function renderPost(post, comments) {
    currentPost = post;
    currentComments = comments;
    
    const container = document.getElementById('postContent');
    const regionName = getRegionName(post.regionCode);
    
    // 현재 사용자 ID 가져오기
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const currentUserId = currentUser.userId;
    const isMyPost = currentUserId && post.userId === currentUserId;
    
    container.innerHTML = `
        <div class="rounded-xl border border-border-dark bg-card-dark p-6 sm:p-8">
            <div class="mb-4 flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div id="postTitleDisplay" class="flex-1">
                    <h1 class="text-2xl font-bold text-white sm:text-3xl">${escapeHtml(post.title || '제목 없음')}</h1>
                </div>
                ${isMyPost ? `
                    <div class="flex items-center gap-2">
                        <button id="editBtn" onclick="editPost()" 
                                class="inline-flex items-center justify-center gap-2 rounded-md bg-blue-500/20 px-3 py-2 text-xs font-medium text-blue-400 transition hover:bg-blue-500/30">
                            <span class="material-symbols-outlined text-sm">edit</span>
                            수정
                        </button>
                        <button onclick="deletePost(${post.postId})" 
                                class="inline-flex items-center justify-center gap-2 rounded-md bg-red-500/20 px-3 py-2 text-xs font-medium text-red-400 transition hover:bg-red-500/30">
                            <span class="material-symbols-outlined text-sm">delete</span>
                            삭제
                        </button>
                    </div>
                ` : ''}
            </div>
            <div class="mb-6 flex items-center gap-4 text-sm text-slate-400">
                <div class="flex items-center gap-2">
                    ${post.authorProfileImage 
                        ? `<div class="size-6 rounded-full bg-cover bg-center bg-no-repeat" style="background-image: url('${post.authorProfileImage}')"></div>`
                        : `<div class="size-6 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                            <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                           </div>`
                    }
                    <span class="font-medium text-white">${post.authorNickname || post.authorName || '작성자'}</span>
                </div>
                <span class="text-slate-600">·</span>
                <div>조회수 ${formatCount(post.viewCount || 0)}</div>
                <span class="text-slate-600">·</span>
                <div>${formatDate(post.createdAt)}</div>
            </div>
            <div class="border-t border-border-dark pt-6">
                <div id="postContentDisplay" class="prose prose-invert min-w-full text-base leading-relaxed text-slate-300">
                    <p class="whitespace-pre-wrap">${escapeHtml(post.content || '')}</p>
                </div>
                <div class="mt-8 flex items-center gap-6 text-slate-400">
                    <button onclick="toggleLike(${post.postId})" class="flex items-center gap-2 transition-colors hover:text-primary">
                        <span class="material-symbols-outlined">thumb_up</span>
                        <span class="text-sm font-medium">${formatCount(post.likeCount || 0)}</span>
                    </button>
                    <div class="flex items-center gap-2">
                        <span class="material-symbols-outlined">chat_bubble</span>
                        <span class="text-sm font-medium">댓글 ${comments.length}</span>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="mt-8 rounded-xl border border-border-dark bg-card-dark p-6 sm:p-8">
            <h2 class="mb-6 text-xl font-bold text-white">댓글</h2>
            <div id="commentsList" class="space-y-6">
                ${renderComments(comments)}
            </div>
            <div class="mt-8 border-t border-border-dark pt-6">
                <div class="flex items-start gap-4">
                    <div class="current-user-avatar size-9 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                        <span class="material-symbols-outlined text-sm text-slate-500">person</span>
                    </div>
                    <div class="flex-grow">
                        <textarea id="commentInput" class="form-textarea w-full resize-none rounded-lg border-border-dark bg-background-dark text-slate-300 placeholder:text-slate-500 focus:border-primary focus:ring-primary/50" placeholder="댓글을 입력하세요..." rows="3"></textarea>
                        <div class="mt-2 flex justify-end">
                            <button onclick="addComment()" class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500">작성</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function renderComments(comments) {
    if (comments.length === 0) {
        return '<p class="text-slate-400 text-center py-4">댓글이 없습니다</p>';
    }
    
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const currentUserId = currentUser.userId;
    
    return comments.map(comment => {
        return renderComment(comment, currentUserId, false);
    }).join('');
}

function renderComment(comment, currentUserId, isReply) {
    const isMyComment = currentUserId && comment.userId === currentUserId;
    const replies = comment.replies || [];
    
    return `
    <div class="${isReply ? 'ml-8 mt-4 border-l-2 border-slate-700 pl-4' : ''}" id="comment-${comment.commentId}">
        <div class="flex gap-4">
            ${comment.authorProfileImage 
                ? `<div class="size-8 flex-shrink-0 rounded-full bg-cover bg-center bg-no-repeat" style="background-image: url('${comment.authorProfileImage}')"></div>`
                : `<div class="size-8 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                    <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                   </div>`
            }
            <div class="flex-grow">
                <div class="flex items-baseline gap-2 justify-between">
                    <div class="flex items-baseline gap-2">
                        <span class="font-semibold text-white text-sm">${escapeHtml(comment.authorNickname || comment.authorName || '작성자')}</span>
                        <span class="text-xs text-slate-500">${formatDate(comment.createdAt)}</span>
                    </div>
                    ${isMyComment ? `
                        <div class="flex items-center gap-2">
                            <button onclick="editComment(${comment.commentId})" 
                                    class="text-xs text-slate-400 hover:text-primary transition-colors flex items-center gap-1">
                                <span class="material-symbols-outlined text-sm">edit</span>
                                <span>수정</span>
                            </button>
                            <button onclick="deleteComment(${comment.commentId})" 
                                    class="text-xs text-slate-400 hover:text-red-400 transition-colors flex items-center gap-1">
                                <span class="material-symbols-outlined text-sm">delete</span>
                                <span>삭제</span>
                            </button>
                        </div>
                    ` : ''}
                </div>
                <div id="comment-content-${comment.commentId}" class="mt-1">
                    <p class="text-slate-300 whitespace-pre-wrap text-sm">${escapeHtml(comment.content)}</p>
                </div>
                <div class="mt-2 flex items-center gap-4">
                    ${!isReply ? `
                        <button onclick="showReplyForm(${comment.commentId})" 
                                class="text-xs text-slate-400 hover:text-primary transition-colors flex items-center gap-1">
                            <span class="material-symbols-outlined text-sm">reply</span>
                            <span>답글</span>
                        </button>
                    ` : ''}
                </div>
                
                ${!isReply ? `
                    <div id="reply-form-${comment.commentId}" class="hidden mt-3 ml-0">
                        <div class="flex items-start gap-3">
                            <div class="current-user-avatar size-7 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                                <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                            </div>
                            <div class="flex-grow">
                                <textarea id="reply-input-${comment.commentId}" 
                                          class="w-full resize-none rounded-lg border border-border-dark bg-background-dark text-slate-300 text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary" 
                                          placeholder="답글을 입력하세요..." 
                                          rows="2"></textarea>
                                <div class="mt-2 flex justify-end gap-2">
                                    <button onclick="cancelReply(${comment.commentId})" 
                                            class="rounded-lg px-3 py-1.5 text-xs font-medium text-slate-400 hover:text-white transition-colors">
                                        취소
                                    </button>
                                    <button onclick="addReply(${comment.commentId})" 
                                            class="rounded-lg bg-primary px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-blue-500">
                                        작성
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div id="replies-${comment.commentId}" class="mt-2">
                        ${replies.length > 0 ? replies.map(reply => renderComment(reply, currentUserId, true)).join('') : ''}
                    </div>
                ` : ''}
            </div>
        </div>
    </div>
    `;
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

// 댓글 수정
function editComment(commentId) {
    const commentElement = document.getElementById(`comment-${commentId}`);
    const contentDiv = document.getElementById(`comment-content-${commentId}`);
    
    if (!commentElement || !contentDiv) return;
    
    // 현재 댓글 내용 가져오기
    const currentComment = currentComments.find(c => c.commentId === commentId);
    if (!currentComment) return;
    
    const currentContent = currentComment.content || '';
    
    // 수정 모드로 변경
    contentDiv.innerHTML = `
        <textarea id="editCommentInput-${commentId}" 
                  class="w-full resize-none rounded-lg border border-border-dark bg-background-dark text-slate-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                  rows="3">${escapeHtml(currentContent)}</textarea>
        <div class="mt-2 flex justify-end gap-2">
            <button onclick="cancelEditComment(${commentId})" 
                    class="rounded-lg px-3 py-1.5 text-sm font-medium text-slate-400 hover:text-white transition-colors">
                취소
            </button>
            <button onclick="saveComment(${commentId})" 
                    class="rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-blue-500">
                저장
            </button>
        </div>
    `;
    
    // 텍스트영역 포커스
    setTimeout(() => {
        const textarea = document.getElementById(`editCommentInput-${commentId}`);
        if (textarea) {
            textarea.focus();
            textarea.setSelectionRange(textarea.value.length, textarea.value.length);
        }
    }, 0);
}

// 댓글 수정 취소
function cancelEditComment(commentId) {
    const currentComment = currentComments.find(c => c.commentId === commentId);
    if (!currentComment) return;
    
    const contentDiv = document.getElementById(`comment-content-${commentId}`);
    if (contentDiv) {
        contentDiv.innerHTML = `<p class="text-slate-300 whitespace-pre-wrap">${escapeHtml(currentComment.content)}</p>`;
    }
}

// 댓글 저장
async function saveComment(commentId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    const textarea = document.getElementById(`editCommentInput-${commentId}`);
    if (!textarea) return;
    
    const content = textarea.value.trim();
    if (!content) {
        alert('댓글 내용을 입력해주세요');
        return;
    }
    
    try {
        const response = await fetch(`/api/board/comments/${commentId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ content: content })
        });
        
        if (!response.ok) {
            console.error('댓글 수정 응답 오류:', response.status, response.statusText);
            const errorText = await response.text();
            console.error('에러 응답 내용:', errorText);
            alert(`댓글 수정에 실패했습니다 (${response.status}): ${errorText}`);
            return;
        }
        
        const result = await response.json();
        
        if (result.success) {
            loadPost(); // 게시글 다시 로드
        } else {
            alert(result.message || '댓글 수정에 실패했습니다');
        }
    } catch (error) {
        console.error('댓글 수정 오류:', error);
        alert('댓글 수정 중 오류가 발생했습니다: ' + error.message);
    }
}

// 댓글 삭제
async function deleteComment(commentId) {
    if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) {
        return;
    }
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        const response = await fetch(`/api/board/comments/${commentId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            console.error('댓글 삭제 응답 오류:', response.status, response.statusText);
            const errorText = await response.text();
            console.error('에러 응답 내용:', errorText);
            alert(`댓글 삭제에 실패했습니다 (${response.status}): ${errorText}`);
            return;
        }
        
        const result = await response.json();
        
        if (result.success) {
            alert('댓글이 삭제되었습니다');
            loadPost(); // 게시글 다시 로드
        } else {
            alert(result.message || '댓글 삭제에 실패했습니다');
        }
    } catch (error) {
        console.error('댓글 삭제 오류:', error);
        alert('댓글 삭제 중 오류가 발생했습니다: ' + error.message);
    }
}

async function toggleLike(postId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    try {
        const response = await fetch(`/api/board/posts/${postId}/like`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            loadPost(); // 새로고침
        } else {
            alert(result.message || '좋아요 처리에 실패했습니다');
        }
    } catch (error) {
        console.error('좋아요 오류:', error);
    }
}

async function addComment() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');
    const content = document.getElementById('commentInput').value;
    
    if (!content.trim()) {
        alert('댓글을 입력해주세요');
        return;
    }
    
    try {
        const response = await fetch(`/api/board/posts/${postId}/comments`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ content: content })
        });
        
        const result = await response.json();
        
        if (result.success) {
            document.getElementById('commentInput').value = '';
            loadPost(); // 새로고침 (아바타도 함께 업데이트됨)
        } else {
            alert(result.message || '댓글 작성에 실패했습니다');
        }
    } catch (error) {
        console.error('댓글 작성 오류:', error);
        alert('댓글 작성 중 오류가 발생했습니다');
    }
}

// 대댓글 폼 보이기
function showReplyForm(commentId) {
    const replyForm = document.getElementById(`reply-form-${commentId}`);
    if (replyForm) {
        replyForm.classList.remove('hidden');
        const textarea = document.getElementById(`reply-input-${commentId}`);
        if (textarea) {
            setTimeout(() => textarea.focus(), 0);
        }
        // 아바타 업데이트
        setTimeout(() => {
            updateCurrentUserAvatar();
        }, 100);
    }
}

// 대댓글 취소
function cancelReply(commentId) {
    const replyForm = document.getElementById(`reply-form-${commentId}`);
    if (replyForm) {
        replyForm.classList.add('hidden');
        const textarea = document.getElementById(`reply-input-${commentId}`);
        if (textarea) {
            textarea.value = '';
        }
    }
}

// 대댓글 작성
async function addReply(parentCommentId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');
    const textarea = document.getElementById(`reply-input-${parentCommentId}`);
    const content = textarea ? textarea.value.trim() : '';
    
    if (!content) {
        alert('답글을 입력해주세요');
        return;
    }
    
    try {
        const response = await fetch(`/api/board/posts/${postId}/comments`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                content: content,
                parentCommentId: parentCommentId
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            loadPost(); // 새로고침
        } else {
            alert(result.message || '답글 작성에 실패했습니다');
        }
    } catch (error) {
        console.error('답글 작성 오류:', error);
        alert('답글 작성 중 오류가 발생했습니다');
    }
}

function scrollToComments() {
    const commentsSection = document.querySelector('.mt-8.rounded-xl.border.border-border-dark');
    if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' });
    }
}

function getRegionName(regionCode) {
    const regionMap = {
        '1': '서울/경기', '2': '부산', '3': '대구', '4': '인천',
        '31': '서울/경기', '32': '강원', '33': '충청', '34': '충청',
        '35': '경상', '36': '경상', '37': '전라', '38': '전라', '39': '제주'
    };
    return regionMap[regionCode] || '';
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
}

function formatCount(count) {
    if (count >= 1000) {
        return (count / 1000).toFixed(1) + '천';
    }
    return count.toString();
}

// 게시글 수정
function editPost() {
    if (!currentPost) return;
    
    const titleDisplay = document.getElementById('postTitleDisplay');
    const contentDisplay = document.getElementById('postContentDisplay');
    const editBtn = document.getElementById('editBtn');
    
    if (!titleDisplay || !contentDisplay) return;
    
    // 수정 모드로 변경
    titleDisplay.innerHTML = `
        <input type="text" id="editPostTitle" 
               class="w-full rounded-lg border border-border-dark bg-background-dark px-4 py-2 text-2xl font-bold text-white focus:outline-none focus:ring-2 focus:ring-primary" 
               value="${escapeHtml(currentPost.title || '')}" />
    `;
    
    contentDisplay.innerHTML = `
        <textarea id="editPostContent" 
                  class="w-full resize-y rounded-lg border border-border-dark bg-background-dark px-4 py-2 text-base leading-relaxed text-slate-300 focus:outline-none focus:ring-2 focus:ring-primary" 
                  rows="10">${escapeHtml(currentPost.content || '')}</textarea>
        <div class="mt-4 flex justify-end gap-2">
            <button onclick="cancelEdit()" 
                    class="rounded-lg px-4 py-2 text-sm font-medium text-slate-400 hover:text-white transition-colors">
                취소
            </button>
            <button onclick="savePost()" 
                    class="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-500">
                저장
            </button>
        </div>
    `;
    
    // 수정 버튼 숨기기
    if (editBtn) {
        editBtn.style.display = 'none';
    }
    
    // 제목 입력 필드에 포커스
    setTimeout(() => {
        const titleInput = document.getElementById('editPostTitle');
        if (titleInput) {
            titleInput.focus();
        }
    }, 0);
}

// 게시글 수정 취소
function cancelEdit() {
    if (!currentPost) return;
    
    renderPost(currentPost, currentComments);
    
    // 아바타 다시 업데이트
    setTimeout(() => {
        updateCurrentUserAvatar();
    }, 100);
}

// 게시글 저장
async function savePost() {
    if (!currentPost) return;
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    const titleInput = document.getElementById('editPostTitle');
    const contentInput = document.getElementById('editPostContent');
    
    if (!titleInput || !contentInput) return;
    
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    
    if (!title) {
        alert('제목을 입력해주세요');
        titleInput.focus();
        return;
    }
    
    if (!content) {
        alert('내용을 입력해주세요');
        contentInput.focus();
        return;
    }
    
    try {
        const response = await fetch(`/api/board/posts/${currentPost.postId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title: title,
                content: content
            })
        });
        
        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다');
            alert('서버 응답 오류: 응답이 비어있습니다');
            return;
        }
        
        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            alert('서버 응답을 파싱할 수 없습니다: ' + e.message);
            return;
        }
        
        if (result.success) {
            alert('게시글이 수정되었습니다');
            loadPost(); // 게시글 다시 로드
        } else {
            alert(result.message || '게시글 수정에 실패했습니다');
        }
    } catch (error) {
        console.error('게시글 수정 오류:', error);
        alert('게시글 수정 중 오류가 발생했습니다: ' + error.message);
    }
}

// 게시글 삭제
async function deletePost(postId) {
    if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
        return;
    }
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        const response = await fetch(`/api/board/posts/${postId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('게시글이 삭제되었습니다');
            window.location.href = 'board.html'; // 게시판으로 이동
        } else {
            alert(result.message || '게시글 삭제에 실패했습니다');
        }
    } catch (error) {
        console.error('게시글 삭제 오류:', error);
        alert('게시글 삭제 중 오류가 발생했습니다');
    }
}

// 현재 사용자 아바타 업데이트
function updateCurrentUserAvatar() {
    const user = localStorage.getItem('user');
    // 모든 current-user-avatar 클래스를 가진 요소 찾기
    const avatarElements = document.querySelectorAll('.current-user-avatar');
    
    if (user && avatarElements.length > 0) {
        try {
            const userData = JSON.parse(user);
            // 모든 아바타 요소 업데이트
            avatarElements.forEach(avatarElement => {
                if (userData.profileImage) {
                    avatarElement.style.backgroundImage = `url('${userData.profileImage}')`;
                    avatarElement.style.backgroundSize = 'cover';
                    avatarElement.style.backgroundPosition = 'center';
                    avatarElement.style.backgroundRepeat = 'no-repeat';
                    avatarElement.innerHTML = '';
                } else {
                    avatarElement.style.backgroundImage = 'none';
                    avatarElement.style.backgroundColor = '';
                    // 크기에 맞는 아이콘 설정
                    const sizeClass = avatarElement.classList.contains('size-7') ? 'text-xs' : 'text-sm';
                    avatarElement.innerHTML = `<span class="material-symbols-outlined ${sizeClass} text-slate-500">person</span>`;
                }
            });
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// 헤더 마이페이지 아이콘 표시
function updateHeaderForLogin() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    const mypageIcon = document.getElementById('mypageIcon');
    
    if (token && user && mypageIcon) {
        try {
            const userData = JSON.parse(user);
            mypageIcon.classList.remove('hidden');
            mypageIcon.style.display = 'block';
            if (userData.profileImage) {
                mypageIcon.style.backgroundImage = `url('${userData.profileImage}')`;
                mypageIcon.style.backgroundSize = 'cover';
                mypageIcon.style.backgroundPosition = 'center';
                mypageIcon.style.backgroundRepeat = 'no-repeat';
            } else {
                mypageIcon.style.backgroundImage = 'none';
                mypageIcon.style.backgroundColor = '#202938';
            }
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// 페이지 로드 시 게시글 로드 및 헤더 업데이트
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        updateHeaderForLogin();
        loadPost();
    });
} else {
    updateHeaderForLogin();
    loadPost();
}


