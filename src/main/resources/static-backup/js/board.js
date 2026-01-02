// 게시판 데이터 로드
let currentPage = 1;
let currentCategory = 'TRAVEL_PLAN'; // 기본값: 국내 여행 공유
let filters = {
    regionCode: null,
    tripType: null,
    season: null,
    sortBy: 'popular'
};

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
            
            // person 아이콘 요소 찾기
            const personIcon = mypageIcon.querySelector('.material-symbols-outlined');
            
            if (userData.profileImage) {
                // 프로필 이미지가 있으면 이미지 표시하고 아이콘 숨김
                mypageIcon.style.backgroundImage = `url('${userData.profileImage}')`;
                mypageIcon.style.backgroundSize = 'cover';
                mypageIcon.style.backgroundPosition = 'center';
                mypageIcon.style.backgroundRepeat = 'no-repeat';
                if (personIcon) {
                    personIcon.style.display = 'none';
                }
            } else {
                // 프로필 이미지가 없으면 기본 배경색과 아이콘 표시
                mypageIcon.style.backgroundImage = 'none';
                mypageIcon.style.backgroundColor = '#202938';
                if (personIcon) {
                    personIcon.style.display = 'block';
                }
            }
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// 탭 버튼 이벤트 리스너
document.getElementById('tabTravelPlan')?.addEventListener('click', () => switchTab('TRAVEL_PLAN'));
document.getElementById('tabTravelRecord')?.addEventListener('click', () => switchTab('TRAVEL_RECORD'));

// 페이지 로드 시 헤더 업데이트 및 초기화
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        updateHeaderForLogin();
        // 초기 탭 설정
        switchTab('TRAVEL_PLAN');
        loadPosts();
    });
} else {
    updateHeaderForLogin();
    // 초기 탭 설정
    switchTab('TRAVEL_PLAN');
    loadPosts();
}

// 모달 외부 클릭 시 닫기
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('recordSelectModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target.id === 'recordSelectModal') {
                closeRecordSelectModal();
            }
        });
    }
});

async function loadPosts() {
    try {
        const token = localStorage.getItem('token');
        const params = new URLSearchParams();
        
        // category 파라미터 추가
        params.append('category', currentCategory);
        
        if (filters.regionCode) params.append('regionCode', filters.regionCode);
        if (filters.tripType) params.append('tripType', filters.tripType);
        if (filters.season) params.append('season', filters.season);
        
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`/api/board/posts?${params.toString()}`, {
            headers: headers
        });
        
        const result = await response.json();
        
        if (result.success) {
            renderPosts(result.data || []);
        } else {
            showEmptyState();
        }
    } catch (error) {
        console.error('게시글 로드 오류:', error);
        showEmptyState();
    }
}

// 탭 전환 함수
function switchTab(category) {
    currentCategory = category;
    
    // 탭 버튼 활성화 상태 변경
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active', 'border-primary', 'text-primary', 'dark:text-primary');
        btn.classList.add('text-slate-600', 'dark:text-slate-400');
    });
    
    // 공유 버튼 텍스트 및 동작 변경
    const shareBtn = document.getElementById('shareBtn');
    const shareBtnText = document.getElementById('shareBtnText');
    
    if (category === 'TRAVEL_PLAN') {
        document.getElementById('tabTravelPlan').classList.add('active', 'border-primary', 'text-primary', 'dark:text-primary');
        document.getElementById('tabTravelPlan').classList.remove('text-slate-600', 'dark:text-slate-400');
        document.getElementById('tabTitle').textContent = '국내 여행 계획 공유';
        document.getElementById('tabDescription').textContent = '다른 사용자들의 여행 계획을 둘러보고 새로운 영감을 얻어보세요.';
        if (shareBtnText) {
            shareBtnText.textContent = '내 여행 계획 공유하기';
        }
        if (shareBtn) {
            shareBtn.onclick = () => window.location.href = 'share-plan.html';
        }
    } else {
        document.getElementById('tabTravelRecord').classList.add('active', 'border-primary', 'text-primary', 'dark:text-primary');
        document.getElementById('tabTravelRecord').classList.remove('text-slate-600', 'dark:text-slate-400');
        document.getElementById('tabTitle').textContent = '여행 기록';
        document.getElementById('tabDescription').textContent = '다른 사용자들의 여행 기록을 둘러보고 함께 여행의 추억을 나눠보세요.';
        if (shareBtnText) {
            shareBtnText.textContent = '내 여행 기록 공유하기';
        }
        if (shareBtn) {
            shareBtn.onclick = openRecordSelectModal;
        }
    }
    
    // 필터 초기화
    filters = {
        regionCode: null,
        tripType: null,
        season: null,
        sortBy: 'popular'
    };
    
    // 체크박스 초기화
    document.querySelectorAll('.region-filter, .trip-type-filter, .season-filter').forEach(cb => {
        cb.checked = false;
    });
    
    // 게시글 다시 로드
    loadPosts();
}

function renderPosts(posts) {
    const container = document.getElementById('postsContainer');
    
    if (posts.length === 0) {
        showEmptyState();
        return;
    }
    
    // 현재 사용자 ID 가져오기
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const currentUserId = currentUser.userId;
    
    const postsHtml = posts.map(post => {
        const regionName = getRegionName(post.regionCode);
        const imageUrl = getPostImage(post);
        const isMyPost = currentUserId && post.userId === currentUserId;
        
        return `
            <div class="group relative flex flex-col overflow-hidden rounded-xl border border-slate-200/10 bg-white/5 dark:border-border-dark dark:bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg dark:hover:shadow-black/20">
                <div class="aspect-video w-full bg-cover bg-center cursor-pointer" style="background-image: url('${imageUrl}')" onclick="viewPost(${post.postId})"></div>
                <div class="flex flex-1 flex-col p-4">
                    <div class="flex items-start justify-between gap-2 mb-2">
                        <h3 class="text-lg font-bold text-slate-800 dark:text-white cursor-pointer flex-1" onclick="viewPost(${post.postId})">${post.title || '제목 없음'}</h3>
                        ${isMyPost ? `
                            <button onclick="event.stopPropagation(); deletePost(${post.postId})" 
                                    class="flex items-center justify-center rounded-lg p-2 text-red-500 hover:bg-red-500/10 transition-colors" 
                                    title="삭제">
                                <span class="material-symbols-outlined text-sm">delete</span>
                            </button>
                        ` : ''}
                    </div>
                    <div class="mt-2 flex flex-wrap gap-2">
                        ${regionName ? `<span class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">${regionName}</span>` : ''}
                        ${post.tripType ? `<span class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">${post.tripType}</span>` : ''}
                        ${post.season ? `<span class="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">${post.season}</span>` : ''}
                    </div>
                    <div class="mt-auto flex items-center justify-between pt-4">
                        <div class="flex items-center gap-2">
                            ${post.authorProfileImage 
                                ? `<div class="size-6 rounded-full bg-cover bg-center bg-no-repeat" style="background-image: url('${post.authorProfileImage}')"></div>`
                                : `<div class="size-6 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center">
                                    <span class="material-symbols-outlined text-xs text-slate-500">person</span>
                                   </div>`
                            }
                            <span class="text-xs font-medium text-slate-500 dark:text-slate-400">${post.authorNickname || post.authorName || '작성자'}</span>
                        </div>
                        <div class="flex items-center gap-4 text-slate-500 dark:text-slate-400">
                            <div class="flex items-center gap-1 cursor-pointer" onclick="event.stopPropagation(); toggleLike(${post.postId})">
                                <span class="material-symbols-outlined text-base">favorite_border</span>
                                <span class="text-xs">${formatCount(post.likeCount || 0)}</span>
                            </div>
                            <div class="flex items-center gap-1 cursor-pointer" onclick="event.stopPropagation(); viewPost(${post.postId})">
                                <span class="material-symbols-outlined text-base">chat_bubble_outline</span>
                                <span class="text-xs">${post.commentCount || 0}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    container.innerHTML = postsHtml;
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
            loadPosts(); // 게시글 목록 새로고침
        } else {
            alert(result.message || '게시글 삭제에 실패했습니다');
        }
    } catch (error) {
        console.error('게시글 삭제 오류:', error);
        alert('게시글 삭제 중 오류가 발생했습니다');
    }
}

function showEmptyState() {
    const container = document.getElementById('postsContainer');
    container.innerHTML = `
        <div class="col-span-full group relative flex flex-col items-center justify-center gap-4 overflow-hidden rounded-xl border-2 border-dashed border-slate-200/20 bg-transparent p-8 text-center dark:border-border-dark">
            <span class="material-symbols-outlined text-4xl text-slate-400 dark:text-slate-500">travel_explore</span>
            <p class="text-sm font-medium text-slate-400 dark:text-slate-500">여행 계획을 찾을 수 없습니다. <br/>필터를 조정해보세요!</p>
        </div>
    `;
}

function getRegionName(regionCode) {
    const regionMap = {
        '1': '서울/경기', '2': '부산', '3': '대구', '4': '인천',
        '31': '서울/경기', '32': '강원', '33': '충청', '34': '충청',
        '35': '경상', '36': '경상', '37': '전라', '38': '전라', '39': '제주'
    };
    return regionMap[regionCode] || '';
}

function getPostImage(post) {
    // 게시글에 연결된 여행 계획의 이미지 또는 기본 이미지
    return 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800';
}

function formatCount(count) {
    if (count >= 1000) {
        return (count / 1000).toFixed(1) + '천';
    }
    return count.toString();
}

function viewPost(postId) {
    window.location.href = `post-detail.html?id=${postId}`;
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
            loadPosts(); // 새로고침
        } else {
            alert(result.message || '좋아요 처리에 실패했습니다');
        }
    } catch (error) {
        console.error('좋아요 오류:', error);
    }
}

// 필터 적용
document.getElementById('searchInput')?.addEventListener('input', (e) => {
    // 검색 기능 (추후 구현)
});

document.getElementById('sortBy')?.addEventListener('change', (e) => {
    filters.sortBy = e.target.value;
    loadPosts();
});

document.querySelectorAll('.region-filter').forEach(checkbox => {
    checkbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            filters.regionCode = e.target.value;
        } else {
            filters.regionCode = null;
        }
        loadPosts();
    });
});

document.querySelectorAll('.trip-type-filter').forEach(checkbox => {
    checkbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            filters.tripType = e.target.value;
        } else {
            filters.tripType = null;
        }
        loadPosts();
    });
});

document.querySelectorAll('.season-filter').forEach(checkbox => {
    checkbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            filters.season = e.target.value;
        } else {
            filters.season = null;
        }
        loadPosts();
    });
});

// 여행 기록 내용 요약 생성
function createRecordSummary(content) {
    if (!content) return '';
    
    // HTML 태그 제거하고 순수 텍스트만 추출
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    const textContent = tempDiv.textContent || tempDiv.innerText || '';
    
    // 연속된 공백과 줄바꿈 정리
    const cleanedText = textContent.replace(/\s+/g, ' ').trim();
    
    // 200자로 제한
    const maxLength = 200;
    if (cleanedText.length <= maxLength) {
        return cleanedText;
    }
    
    return cleanedText.substring(0, maxLength) + '...';
}

// 여행 기록 선택 모달 열기
async function openRecordSelectModal() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    const modal = document.getElementById('recordSelectModal');
    if (!modal) return;
    
    modal.classList.remove('hidden');
    
    // 여행 기록 목록 로드
    try {
        const response = await fetch('/api/records/user', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        const recordsList = document.getElementById('recordsList');
        
        if (result.success && result.data && result.data.length > 0) {
            const records = result.data;
            recordsList.innerHTML = records.map(record => {
                // 요약본 생성
                const summary = createRecordSummary(record.content);
                
                return `
                <div onclick="shareRecord(${record.recordId})" class="flex items-center gap-4 p-4 rounded-lg border border-border-dark bg-background-dark hover:bg-card-dark cursor-pointer transition-colors">
                    <div class="flex-1">
                        <h4 class="text-white font-semibold mb-1">${record.title || '제목 없음'}</h4>
                        <p class="text-sm text-slate-400 line-clamp-2">${summary || '내용 없음'}</p>
                        <p class="text-xs text-slate-500 mt-2">${formatDate(record.createdAt)}</p>
                    </div>
                    <span class="material-symbols-outlined text-slate-400">chevron_right</span>
                </div>
            `;
            }).join('');
        } else {
            recordsList.innerHTML = `
                <div class="text-center py-8 text-slate-400">
                    <span class="material-symbols-outlined text-4xl mb-2">book</span>
                    <p>공유할 여행 기록이 없습니다.</p>
                    <a href="record.html" class="mt-4 inline-block text-primary hover:underline">새 여행 기록 작성하기</a>
                </div>
            `;
        }
    } catch (error) {
        console.error('여행 기록 로드 오류:', error);
        const recordsList = document.getElementById('recordsList');
        recordsList.innerHTML = `
            <div class="text-center py-8 text-red-400">
                <p>여행 기록을 불러오는 중 오류가 발생했습니다.</p>
            </div>
        `;
    }
}

// 여행 기록 선택 모달 닫기
function closeRecordSelectModal() {
    const modal = document.getElementById('recordSelectModal');
    if (modal) {
        modal.classList.add('hidden');
    }
}

// 여행 기록 게시판에 공유
async function shareRecord(recordId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        // 여행 기록 상세 정보 가져오기
        const recordResponse = await fetch(`/api/records/${recordId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const recordResult = await recordResponse.json();
        
        if (!recordResult.success) {
            alert('여행 기록을 불러올 수 없습니다');
            return;
        }
        
        const record = recordResult.data;
        
        if (confirm(`"${record.title || '여행 기록'}"을(를) 게시판에 공유하시겠습니까?`)) {
            const shareResponse = await fetch('/api/board/posts', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    planId: record.planId,
                    title: record.title,
                    content: record.content, // 전체 내용 그대로 저장
                    regionCode: null,
                    tripType: null,
                    season: null,
                    category: 'TRAVEL_RECORD'
                })
            });
            
            const shareResult = await shareResponse.json();
            
            if (shareResult.success) {
                alert('게시판에 공유되었습니다!');
                closeRecordSelectModal();
                loadPosts(); // 게시글 목록 새로고침
            } else {
                alert(shareResult.message || '공유에 실패했습니다');
            }
        }
    } catch (error) {
        console.error('여행 기록 공유 오류:', error);
        alert('공유 중 오류가 발생했습니다');
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
}


