// 나의 여행 기록 목록 페이지

// 여행 기록 목록 로드
async function loadRecords() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    try {
        const response = await fetch('/api/records/user', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            const records = result.data || [];
            // 각 기록의 첫 번째 이미지 로드
            const recordsWithImages = await loadRecordImages(records, token);
            renderRecords(recordsWithImages);
        } else {
            showEmptyState();
        }
    } catch (error) {
        console.error('여행 기록 로드 오류:', error);
        showEmptyState();
    }
}

// 각 기록의 첫 번째 이미지 로드
async function loadRecordImages(records, token) {
    const recordsWithImages = await Promise.all(
        records.map(async (record) => {
            try {
                const response = await fetch(`/api/records/${record.recordId}/images`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                const result = await response.json();
                const images = result.success ? (result.data || []) : [];
                const firstImageUrl = images.length > 0 ? images[0].imageUrl : null;
                
                if (firstImageUrl) {
                    console.log(`기록 ${record.recordId} 첫 번째 이미지 URL 로드됨:`, firstImageUrl.substring(0, 50) + '...');
                }
                
                return {
                    ...record,
                    firstImageUrl: firstImageUrl
                };
            } catch (error) {
                console.error(`기록 ${record.recordId} 이미지 로드 오류:`, error);
                return {
                    ...record,
                    firstImageUrl: null
                };
            }
        })
    );
    return recordsWithImages;
}

// 여행 기록 렌더링
function renderRecords(records) {
    const container = document.getElementById('recordsContainer');
    const emptyState = document.getElementById('emptyState');
    
    if (records.length === 0) {
        container.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }
    
    emptyState.classList.add('hidden');
    
    container.innerHTML = records.map(record => {
        const date = new Date(record.createdAt);
        const dateStr = date.toLocaleDateString('ko-KR', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
        
        // HTML 태그 제거하고 순수 텍스트만 추출
        let contentText = '';
        if (record.content) {
            // 임시 div 생성하여 HTML 파싱
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = record.content;
            contentText = tempDiv.textContent || tempDiv.innerText || '';
            // 연속된 공백과 줄바꿈 정리
            contentText = contentText.replace(/\s+/g, ' ').trim();
        }
        
        const contentPreview = contentText
            ? (contentText.length > 100 ? contentText.substring(0, 100) + '...' : contentText)
            : '내용 없음';
        
        // 이미지 표시 (첫 번째 이미지만 표시, 모든 기록에 동일한 스타일)
        const imageHtml = record.firstImageUrl
            ? `<div class="aspect-video w-full bg-cover bg-center rounded-t-xl mb-4" style="background-image: url('${record.firstImageUrl}')"></div>`
            : `<div class="aspect-video w-full bg-gray-700 rounded-t-xl mb-4 flex items-center justify-center">
                <span class="material-symbols-outlined text-4xl text-gray-500">image</span>
               </div>`;
        
        return `
            <div class="group relative flex flex-col overflow-hidden rounded-xl border border-border-dark bg-card-dark shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg hover:border-primary/50 cursor-pointer" onclick="viewRecord(${record.recordId})">
                ${imageHtml}
                <div class="flex flex-1 flex-col p-4">
                    <div class="flex items-start justify-between mb-2">
                        <h3 class="text-lg font-bold text-text-dark line-clamp-2 flex-1">${record.title || '제목 없음'}</h3>
                        <button onclick="event.stopPropagation(); deleteRecord(${record.recordId})" class="ml-2 text-text-secondary-dark hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100">
                            <span class="material-symbols-outlined text-sm">delete</span>
                        </button>
                    </div>
                    <p class="text-text-secondary-dark text-sm mb-4 line-clamp-2">${escapeHtml(contentPreview)}</p>
                    <div class="mt-auto flex items-center justify-between text-xs text-text-secondary-dark">
                        <span>${dateStr}</span>
                        ${record.planId ? '<span class="px-2 py-1 rounded bg-primary/20 text-primary text-xs">계획 연결됨</span>' : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

// 빈 상태 표시
function showEmptyState() {
    const container = document.getElementById('recordsContainer');
    const emptyState = document.getElementById('emptyState');
    container.innerHTML = '';
    emptyState.classList.remove('hidden');
}

// 여행 기록 상세 보기
function viewRecord(recordId) {
    window.location.href = `record-detail.html?id=${recordId}`;
}

// 여행 기록 삭제
async function deleteRecord(recordId) {
    if (!confirm('정말로 이 여행 기록을 삭제하시겠습니까?')) {
        return;
    }
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        const response = await fetch(`/api/records/${recordId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('여행 기록이 삭제되었습니다');
            loadRecords();
        } else {
            alert(result.message || '삭제에 실패했습니다');
        }
    } catch (error) {
        console.error('삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다');
    }
}

// 새 여행 기록 버튼 클릭
document.getElementById('newRecordBtn')?.addEventListener('click', () => {
    openPlanSelectModal();
});

// 대시보드 선택 모달 열기
async function openPlanSelectModal() {
    const modal = document.getElementById('planSelectModal');
    const planSelect = document.getElementById('modalPlanSelect');
    
    // 여행 계획 목록 로드
    const token = localStorage.getItem('token');
    if (token) {
        try {
            const response = await fetch('/api/plans', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            const result = await response.json();
            
            if (result.success) {
                const plans = result.data || [];
                planSelect.innerHTML = '<option value="">선택 안함</option>';
                plans.forEach(plan => {
                    const option = document.createElement('option');
                    option.value = plan.planId;
                    option.textContent = plan.title || `여행 계획 ${plan.planId}`;
                    planSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('여행 계획 로드 오류:', error);
        }
    }
    
    modal.classList.remove('hidden');
}

// 대시보드 선택 모달 닫기
function closePlanSelectModal() {
    const modal = document.getElementById('planSelectModal');
    modal.classList.add('hidden');
}

// 여행 기록 작성 페이지로 이동
function goToWriteRecord() {
    const planId = document.getElementById('modalPlanSelect').value;
    const url = planId 
        ? `record.html?planId=${planId}`
        : 'record.html';
    
    closePlanSelectModal();
    window.location.href = url;
}

// 헤더 마이페이지 아이콘 표시
function updateHeaderForLogin() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    const mypageIcon = document.getElementById('mypageIcon');
    const mypageIconMobile = document.getElementById('mypageIconMobile');
    
    if (token && user) {
        try {
            const userData = JSON.parse(user);
            
            if (mypageIcon) {
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
            }
            
            if (mypageIconMobile) {
                mypageIconMobile.classList.remove('hidden');
                mypageIconMobile.style.display = 'flex';
                
                // person 아이콘 요소 찾기
                const personIconMobile = mypageIconMobile.querySelector('.material-symbols-outlined');
                
                if (userData.profileImage) {
                    // 프로필 이미지가 있으면 이미지 표시하고 아이콘 숨김
                    mypageIconMobile.style.backgroundImage = `url('${userData.profileImage}')`;
                    mypageIconMobile.style.backgroundSize = 'cover';
                    mypageIconMobile.style.backgroundPosition = 'center';
                    mypageIconMobile.style.backgroundRepeat = 'no-repeat';
                    if (personIconMobile) {
                        personIconMobile.style.display = 'none';
                    }
                } else {
                    // 프로필 이미지가 없으면 기본 배경색과 아이콘 표시
                    mypageIconMobile.style.backgroundImage = 'none';
                    mypageIconMobile.style.backgroundColor = '#202938';
                    if (personIconMobile) {
                        personIconMobile.style.display = 'block';
                    }
                }
            }
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// 페이지 로드 시 여행 기록 목록 로드
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        loadRecords();
        updateHeaderForLogin();
    });
} else {
    loadRecords();
    updateHeaderForLogin();
}

