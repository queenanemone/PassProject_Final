// 여행 기록 상세 보기
let currentRecordId = null;

// 페이지 로드 시 여행 기록 로드
async function loadRecord() {
    const urlParams = new URLSearchParams(window.location.search);
    const recordId = urlParams.get('id');
    
    if (!recordId) {
        alert('여행 기록 ID가 없습니다');
        goBack();
        return;
    }
    
    currentRecordId = recordId;
    
    try {
        const token = localStorage.getItem('token');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        // 여행 기록 정보와 이미지를 동시에 가져오기
        const [recordResponse, imagesResponse] = await Promise.all([
            fetch(`/api/records/${recordId}`, { headers }),
            fetch(`/api/records/${recordId}/images`, { headers })
        ]);
        
        const recordResult = await recordResponse.json();
        const imagesResult = await imagesResponse.json();
        
        console.log('여행 기록 로드 결과:', { recordResult, imagesResult });
        
        if (recordResult.success) {
            const images = imagesResult.success ? imagesResult.data || [] : [];
            console.log(`이미지 ${images.length}개 로드됨:`, images);
            renderRecord(recordResult.data, images);
        } else {
            alert('여행 기록을 불러올 수 없습니다');
            goBack();
        }
    } catch (error) {
        console.error('여행 기록 로드 오류:', error);
        alert('여행 기록을 불러오는 중 오류가 발생했습니다');
        goBack();
    }
}

// 여행 기록 렌더링
function renderRecord(record, images) {
    const container = document.getElementById('recordContent');
    const date = new Date(record.createdAt);
    const dateStr = date.toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
    
    // 이미지 HTML 생성
    console.log('이미지 렌더링 시작:', images);
    const imagesHtml = images.length > 0 
        ? `
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                ${images.map((img, index) => {
                    const imageUrl = img.imageUrl || img.image_url || '';
                    console.log(`이미지 ${index + 1} URL:`, imageUrl ? imageUrl.substring(0, 50) + '...' : '없음');
                    return `
                    <div class="relative group rounded-lg overflow-hidden bg-gray-800">
                        <img src="${imageUrl}" alt="여행 기록 이미지 ${index + 1}" 
                             class="w-full h-64 object-cover cursor-pointer hover:opacity-90 transition-opacity"
                             onerror="console.error('이미지 로드 실패:', this.src); this.style.display='none';"
                             onclick="openImageModal('${imageUrl.replace(/'/g, "\\'")}')"/>
                    </div>
                `;
                }).join('')}
            </div>
        `
        : '<div class="text-text-secondary-dark text-sm mb-6">이미지가 없습니다.</div>';
    
    container.innerHTML = `
        <article class="bg-card-dark rounded-xl border border-border-dark p-6 sm:p-8 space-y-6">
            <div>
                <h1 class="text-3xl md:text-4xl font-extrabold text-text-dark mb-4">${record.title || '제목 없음'}</h1>
                <div class="flex items-center gap-4 text-sm text-text-secondary-dark">
                    <span>${dateStr}</span>
                    ${record.planId ? '<span class="px-3 py-1 rounded-full bg-primary/20 text-primary text-xs font-semibold">계획 연결됨</span>' : ''}
                </div>
            </div>
            
            ${imagesHtml}
            
            <div class="prose prose-invert max-w-none">
                <p class="text-text-dark whitespace-pre-wrap text-base leading-relaxed">${record.content || ''}</p>
            </div>
            
            <div class="flex items-center gap-3 pt-4 border-t border-border-dark">
                <button onclick="editRecord()" class="flex items-center gap-2 px-4 py-2 rounded-lg bg-secondary-button-dark text-text-dark text-sm font-semibold hover:bg-gray-600 transition-colors">
                    <span class="material-symbols-outlined text-base">edit</span>
                    <span>수정</span>
                </button>
                <button onclick="deleteRecord()" class="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/20 text-red-400 text-sm font-semibold hover:bg-red-500/30 transition-colors">
                    <span class="material-symbols-outlined text-base">delete</span>
                    <span>삭제</span>
                </button>
                <button onclick="shareToBoard()" class="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-bold hover:bg-blue-400 transition-colors">
                    <span class="material-symbols-outlined text-base">share</span>
                    <span>게시판에 공유</span>
                </button>
            </div>
        </article>
    `;
}

// 이미지 모달 열기
function openImageModal(imageUrl) {
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-sm';
    modal.innerHTML = `
        <div class="relative max-w-4xl max-h-full p-4">
            <button onclick="this.closest('.fixed').remove()" class="absolute top-4 right-4 text-white hover:text-gray-300 z-10">
                <span class="material-symbols-outlined text-3xl">close</span>
            </button>
            <img src="${imageUrl}" alt="확대 이미지" class="max-w-full max-h-[90vh] object-contain rounded-lg"/>
        </div>
    `;
    modal.onclick = (e) => {
        if (e.target === modal) modal.remove();
    };
    document.body.appendChild(modal);
}

// 여행 기록 수정
function editRecord() {
    window.location.href = `record.html?edit=true&id=${currentRecordId}`;
}

// 여행 기록 삭제
async function deleteRecord() {
    if (!confirm('정말로 이 여행 기록을 삭제하시겠습니까?')) {
        return;
    }
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        const response = await fetch(`/api/records/${currentRecordId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('여행 기록이 삭제되었습니다');
            goBack();
        } else {
            alert(result.message || '삭제에 실패했습니다');
        }
    } catch (error) {
        console.error('삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다');
    }
}

// 게시판에 공유
async function shareToBoard() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    // 먼저 여행 기록 정보를 다시 가져옴
    try {
        const response = await fetch(`/api/records/${currentRecordId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (!result.success) {
            alert('여행 기록을 불러올 수 없습니다');
            return;
        }
        
        const record = result.data;
        
        if (confirm('게시판에 이 여행 기록을 공유하시겠습니까?')) {
            try {
                const shareResponse = await fetch('/api/board/posts', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        planId: record.planId,
                        title: record.title,
                        content: record.content,
                        regionCode: null,
                        tripType: null,
                        season: null,
                        category: 'TRAVEL_RECORD'
                    })
                });
                
                const shareResult = await shareResponse.json();
                
                if (shareResult.success) {
                    alert('게시판에 공유되었습니다!');
                } else {
                    alert(shareResult.message || '공유에 실패했습니다');
                }
            } catch (error) {
                console.error('게시판 공유 오류:', error);
                alert('게시판 공유 중 오류가 발생했습니다');
            }
        }
    } catch (error) {
        console.error('오류:', error);
        alert('오류가 발생했습니다');
    }
}

// 뒤로가기
function goBack() {
    window.location.href = 'my-records.html';
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
            mypageIcon.style.display = 'flex';
            
            // person 아이콘 요소 찾기
            const personIcon = mypageIcon.querySelector('.material-symbols-outlined');
            
            if (userData.profileImage) {
                // 프로필 이미지가 있으면 이미지 표시하고 아이콘 숨김
                mypageIcon.style.backgroundImage = `url('${userData.profileImage}')`;
                mypageIcon.style.backgroundSize = 'cover';
                mypageIcon.style.backgroundPosition = 'center';
                mypageIcon.style.backgroundRepeat = 'no-repeat';
                // 아이콘 내부 HTML 제거하여 아이콘 완전히 숨김
                mypageIcon.innerHTML = '';
            } else {
                // 프로필 이미지가 없으면 기본 배경색과 아이콘 표시
                mypageIcon.style.backgroundImage = 'none';
                mypageIcon.style.backgroundColor = '#202938';
                if (personIcon) {
                    personIcon.style.display = 'block';
                } else {
                    // 아이콘이 없으면 다시 추가
                    mypageIcon.innerHTML = '<span class="material-symbols-outlined">person</span>';
                }
            }
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// 페이지 로드 시 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        loadRecord();
        updateHeaderForLogin();
    });
} else {
    loadRecord();
    updateHeaderForLogin();
}

