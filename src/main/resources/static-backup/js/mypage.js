// 마이페이지

// 페이지 로드 시 사용자 정보 및 여행 성향 로드
async function loadUserInfo() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    try {
        const response = await fetch('/api/user/info', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            renderUserInfo(result.data);
        } else {
            alert('사용자 정보를 불러올 수 없습니다');
        }
    } catch (error) {
        console.error('사용자 정보 로드 오류:', error);
        alert('사용자 정보를 불러오는 중 오류가 발생했습니다');
    }
}

// 사용자 정보 렌더링
function renderUserInfo(user) {
    const container = document.getElementById('userInfo');
    
    // 구글 로그인 여부 확인
    // localStorage의 user 정보에서 확인하거나, password가 null/빈 문자열인 경우
    const storedUser = localStorage.getItem('user');
    let isGoogleUser = false;
    if (storedUser) {
        try {
            const userData = JSON.parse(storedUser);
            // password가 빈 문자열이거나 없는 경우 구글 로그인으로 판단
            isGoogleUser = !userData.password || userData.password === '';
        } catch (e) {
            // 파싱 실패 시 password 필드로 확인
            isGoogleUser = !user.password || user.password === '';
        }
    } else {
        isGoogleUser = !user.password || user.password === '';
    }
    
    container.innerHTML = `
        <div class="flex items-center gap-4 mb-4">
            <div class="relative">
                ${user.profileImage 
                    ? `<img id="profileImagePreview" src="${user.profileImage}" alt="프로필" class="w-20 h-20 rounded-full object-cover border-2 border-primary"/>`
                    : `<div id="profileImagePreview" class="w-20 h-20 rounded-full bg-primary/20 flex items-center justify-center border-2 border-primary">
                        <span class="material-symbols-outlined text-4xl text-primary">person</span>
                       </div>`
                }
                ${!isGoogleUser ? `
                    <label for="profileImageInput" class="absolute bottom-0 right-0 w-6 h-6 rounded-full bg-primary flex items-center justify-center cursor-pointer hover:bg-blue-400 transition-colors border-2 border-background-dark">
                        <span class="material-symbols-outlined text-sm text-white">camera_alt</span>
                    </label>
                    <input type="file" id="profileImageInput" accept="image/*" class="hidden" onchange="handleProfileImageChange(event)"/>
                ` : ''}
            </div>
            <div>
                <h3 class="text-xl font-bold text-text-dark">${user.name || user.nickname || '사용자'}</h3>
                ${user.nickname && user.nickname !== user.name ? `<p class="text-text-secondary-dark">${user.nickname}</p>` : ''}
                ${isGoogleUser ? '<span class="inline-block mt-1 px-2 py-1 rounded bg-blue-500/20 text-blue-400 text-xs">Google 로그인</span>' : ''}
            </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">이메일</label>
                <p class="text-text-dark font-medium">${user.email || '-'}</p>
            </div>
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">이름</label>
                <p class="text-text-dark font-medium">${user.name || '-'}</p>
            </div>
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">닉네임</label>
                <div id="nicknameView" class="flex items-center gap-0">
                    <p class="text-text-dark font-medium">${user.nickname || '닉네임이 없습니다'}</p>
                    <button onclick="editNickname()" 
                            class="flex items-center justify-center gap-1 rounded-lg h-8 px-3 bg-card-dark text-text-dark border border-border-dark hover:bg-card-dark/80 transition-colors ml-2">
                        <span class="material-symbols-outlined text-sm">edit</span>
                        <span class="text-sm">수정</span>
                    </button>
                </div>
                <div id="nicknameEdit" class="hidden flex items-center gap-2">
                    <input type="text" id="nicknameInput" value="${user.nickname || ''}" 
                           placeholder="닉네임을 입력하세요"
                           class="flex-1 rounded-lg text-text-dark focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-border-dark bg-background-dark h-10 px-4"/>
                    <button onclick="saveNickname()" 
                            class="flex items-center justify-center rounded-lg h-10 px-3 bg-primary text-white font-medium hover:bg-blue-400 transition-colors">
                        <span class="text-sm">저장</span>
                    </button>
                    <button onclick="cancelEditNickname()" 
                            class="flex items-center justify-center gap-1 rounded-lg h-10 px-3 bg-card-dark text-text-dark border border-border-dark hover:bg-card-dark/80 transition-colors">
                        <span class="text-sm">취소</span>
                    </button>
                </div>
            </div>
            ${user.phone ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">전화번호</label>
                <p class="text-text-dark font-medium">${user.phone}</p>
            </div>
            ` : ''}
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">가입일</label>
                <p class="text-text-dark font-medium">${user.createdAt ? new Date(user.createdAt).toLocaleDateString('ko-KR') : '-'}</p>
            </div>
        </div>
    `;
}

// 여행 성향 로드
async function loadPreference() {
    const token = localStorage.getItem('token');
    if (!token) {
        return;
    }
    
    try {
        const response = await fetch('/api/user/preference', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        
        if (result.success && result.data) {
            fillPreferenceForm(result.data);
            // 저장된 데이터가 있으면 읽기 모드로 표시
            switchToViewMode(result.data);
        } else {
            // 저장된 데이터가 없으면 편집 모드 유지
            switchToEditMode();
        }
    } catch (error) {
        console.error('여행 성향 로드 오류:', error);
        // 오류 시 편집 모드 유지
        switchToEditMode();
    }
}

// 여행 성향 폼 채우기
function fillPreferenceForm(preference) {
    if (preference.preferredTripType) {
        document.getElementById('preferredTripType').value = preference.preferredTripType;
    }
    
    if (preference.preferredActivities) {
        try {
            const activities = JSON.parse(preference.preferredActivities);
            activities.forEach(activity => {
                const checkbox = document.querySelector(`input[name="preferredActivities"][value="${activity}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        } catch (e) {
            // JSON 파싱 실패 시 쉼표로 구분된 문자열로 처리
            const activities = preference.preferredActivities.split(',').map(a => a.trim());
            activities.forEach(activity => {
                const checkbox = document.querySelector(`input[name="preferredActivities"][value="${activity}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
    }
    
    if (preference.budgetPreference) {
        document.getElementById('budgetPreference').value = preference.budgetPreference;
    }
    if (preference.accommodationPreference) {
        document.getElementById('accommodationPreference').value = preference.accommodationPreference;
    }
    if (preference.seasonPreference) {
        document.getElementById('seasonPreference').value = preference.seasonPreference;
    }
    if (preference.transportationPreference) {
        document.getElementById('transportationPreference').value = preference.transportationPreference;
    }
    if (preference.foodPreference) {
        document.getElementById('foodPreference').value = preference.foodPreference;
    }
    if (preference.travelStyle) {
        document.getElementById('travelStyle').value = preference.travelStyle;
    }
    if (preference.additionalInfo) {
        document.getElementById('additionalInfo').value = preference.additionalInfo;
    }
}

// 여행 성향 저장
document.getElementById('preferenceForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    const submitButton = e.target.querySelector('button[type="submit"]');
    const originalButtonText = submitButton.innerHTML;
    
    // 저장 중 상태로 변경
    submitButton.disabled = true;
    submitButton.innerHTML = `
        <span class="material-symbols-outlined animate-spin">sync</span>
        <span>저장 중...</span>
    `;
    submitButton.classList.remove('hover:bg-blue-400');
    submitButton.classList.add('opacity-75', 'cursor-not-allowed');
    
    // 체크박스에서 선택된 활동 가져오기
    const activityCheckboxes = document.querySelectorAll('input[name="preferredActivities"]:checked');
    const preferredActivities = Array.from(activityCheckboxes).map(cb => cb.value);
    
    const preferenceData = {
        preferredTripType: document.getElementById('preferredTripType').value || null,
        preferredActivities: preferredActivities.length > 0 ? JSON.stringify(preferredActivities) : null,
        budgetPreference: document.getElementById('budgetPreference').value || null,
        accommodationPreference: document.getElementById('accommodationPreference').value || null,
        seasonPreference: document.getElementById('seasonPreference').value || null,
        transportationPreference: document.getElementById('transportationPreference').value || null,
        foodPreference: document.getElementById('foodPreference').value || null,
        travelStyle: document.getElementById('travelStyle').value || null,
        additionalInfo: document.getElementById('additionalInfo').value || null
    };
    
    try {
        const response = await fetch('/api/user/preference', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(preferenceData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            // 성공 메시지 표시
            showSuccessMessage('여행 성향이 저장되었습니다!');
            
            // 저장된 데이터로 읽기 모드로 전환
            if (result.data) {
                fillPreferenceForm(result.data);
                switchToViewMode(result.data);
            }
        } else {
            // 저장 실패 시 원래 상태로 복구
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
            submitButton.classList.remove('opacity-75', 'cursor-not-allowed');
            submitButton.classList.add('hover:bg-blue-400');
            
            alert(result.message || '저장에 실패했습니다');
        }
    } catch (error) {
        console.error('여행 성향 저장 오류:', error);
        
        // 오류 시 원래 상태로 복구
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
        submitButton.classList.remove('opacity-75', 'cursor-not-allowed');
        submitButton.classList.add('hover:bg-blue-400');
        
        alert('저장 중 오류가 발생했습니다');
    }
});

// 읽기 모드로 전환
function switchToViewMode(preference) {
    const form = document.getElementById('preferenceForm');
    if (!form) return;
    
    // 폼 요소들 숨기기
    const formElements = form.querySelectorAll('select, input[type="checkbox"], textarea');
    formElements.forEach(el => {
        el.style.display = 'none';
    });
    
    // 모든 라벨 숨기기
    const labels = form.querySelectorAll('label');
    labels.forEach(label => {
        label.style.display = 'none';
    });
    
    // 읽기 모드 컨테이너 생성
    let viewContainer = document.getElementById('preferenceViewContainer');
    if (!viewContainer) {
        viewContainer = document.createElement('div');
        viewContainer.id = 'preferenceViewContainer';
        viewContainer.className = 'space-y-6';
        form.insertBefore(viewContainer, form.firstChild);
    }
    
    // 선택된 활동 파싱
    let activities = [];
    if (preference.preferredActivities) {
        try {
            activities = JSON.parse(preference.preferredActivities);
        } catch (e) {
            activities = preference.preferredActivities.split(',').map(a => a.trim());
        }
    }
    
    // 읽기 모드 HTML 생성
    viewContainer.innerHTML = `
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            ${preference.preferredTripType ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">선호하는 여행 타입</label>
                <p class="text-text-dark font-medium">${preference.preferredTripType}</p>
            </div>
            ` : ''}
            
            ${activities.length > 0 ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">선호하는 활동</label>
                <p class="text-text-dark font-medium">${activities.join(', ')}</p>
            </div>
            ` : ''}
            
            ${preference.budgetPreference ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">예산 선호도</label>
                <p class="text-text-dark font-medium">${preference.budgetPreference}</p>
            </div>
            ` : ''}
            
            ${preference.accommodationPreference ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">숙소 선호도</label>
                <p class="text-text-dark font-medium">${preference.accommodationPreference}</p>
            </div>
            ` : ''}
            
            ${preference.seasonPreference ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">계절 선호도</label>
                <p class="text-text-dark font-medium">${preference.seasonPreference}</p>
            </div>
            ` : ''}
            
            ${preference.transportationPreference ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">교통 수단 선호도</label>
                <p class="text-text-dark font-medium">${preference.transportationPreference}</p>
            </div>
            ` : ''}
            
            ${preference.foodPreference ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">음식 선호도</label>
                <p class="text-text-dark font-medium">${preference.foodPreference}</p>
            </div>
            ` : ''}
            
            ${preference.travelStyle ? `
            <div>
                <label class="text-text-secondary-dark text-sm mb-1 block">여행 스타일</label>
                <p class="text-text-dark font-medium">${preference.travelStyle}</p>
            </div>
            ` : ''}
        </div>
        
        ${preference.additionalInfo ? `
        <div>
            <label class="text-text-secondary-dark text-sm mb-1 block">추가 정보</label>
            <p class="text-text-dark font-medium whitespace-pre-wrap">${preference.additionalInfo}</p>
        </div>
        ` : ''}
    `;
    
    // 저장 버튼을 수정하기 버튼으로 변경
    const submitButton = form.querySelector('button[type="submit"], button[type="button"]');
    if (submitButton) {
        submitButton.type = 'button';
        submitButton.innerHTML = `
            <span class="material-symbols-outlined">edit</span>
            <span>수정하기</span>
        `;
        // 기존 이벤트 리스너 제거 후 새로 추가
        submitButton.onclick = (e) => {
            e.preventDefault();
            switchToEditMode();
        };
    }
}

// 편집 모드로 전환
function switchToEditMode() {
    const form = document.getElementById('preferenceForm');
    if (!form) return;
    
    // 읽기 모드 컨테이너 제거
    const viewContainer = document.getElementById('preferenceViewContainer');
    if (viewContainer) {
        viewContainer.remove();
    }
    
    // 폼 요소들 다시 표시
    const formElements = form.querySelectorAll('select, input[type="checkbox"], textarea');
    formElements.forEach(el => {
        el.style.display = '';
    });
    
    // 라벨들 다시 표시
    const labels = form.querySelectorAll('label');
    labels.forEach(label => {
        label.style.display = '';
    });
    
    // 수정하기 버튼을 저장하기 버튼으로 변경
    const editButton = form.querySelector('button[type="button"], button[type="submit"]');
    if (editButton) {
        editButton.type = 'submit';
        editButton.innerHTML = `
            <span class="material-symbols-outlined">save</span>
            <span>저장하기</span>
        `;
        editButton.onclick = null;
        // 클래스 초기화
        editButton.classList.remove('bg-green-500', 'hover:bg-green-600', 'opacity-75', 'cursor-not-allowed');
        editButton.classList.add('bg-primary', 'hover:bg-blue-400');
        editButton.disabled = false;
    }
}

// 성공 메시지 표시 함수
function showSuccessMessage(message) {
    // 기존 메시지 제거
    const existingMessage = document.getElementById('successMessage');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // 성공 메시지 생성
    const successMessage = document.createElement('div');
    successMessage.id = 'successMessage';
    successMessage.className = 'fixed top-20 right-4 z-50 bg-green-500 text-white px-6 py-4 rounded-lg shadow-lg flex items-center gap-3 animate-slide-in';
    successMessage.innerHTML = `
        <span class="material-symbols-outlined">check_circle</span>
        <span class="font-semibold">${message}</span>
    `;
    
    document.body.appendChild(successMessage);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        successMessage.style.animation = 'fade-out 0.3s ease-out';
        setTimeout(() => {
            successMessage.remove();
        }, 300);
    }, 3000);
}

// 닉네임 편집 모드로 전환
function editNickname() {
    const nicknameView = document.getElementById('nicknameView');
    const nicknameEdit = document.getElementById('nicknameEdit');
    const nicknameInput = document.getElementById('nicknameInput');
    
    if (nicknameView && nicknameEdit) {
        nicknameView.classList.add('hidden');
        nicknameEdit.classList.remove('hidden');
        nicknameInput.focus();
        nicknameInput.select();
    }
}

// 닉네임 편집 취소
function cancelEditNickname() {
    const nicknameView = document.getElementById('nicknameView');
    const nicknameEdit = document.getElementById('nicknameEdit');
    const nicknameInput = document.getElementById('nicknameInput');
    
    if (nicknameView && nicknameEdit) {
        // 원래 값으로 복구
        const originalNickname = nicknameView.querySelector('p').textContent;
        nicknameInput.value = originalNickname === '닉네임이 없습니다' ? '' : originalNickname;
        
        nicknameView.classList.remove('hidden');
        nicknameEdit.classList.add('hidden');
    }
}

// 닉네임 저장
async function saveNickname() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    const nicknameInput = document.getElementById('nicknameInput');
    const nickname = nicknameInput.value.trim();
    
    if (!nickname) {
        alert('닉네임을 입력해주세요');
        nicknameInput.focus();
        return;
    }
    
    const saveButton = event.target.closest('button');
    const originalButtonText = saveButton.innerHTML;
    
    // 저장 중 상태로 변경
    saveButton.disabled = true;
    saveButton.innerHTML = `
        <span class="material-symbols-outlined text-sm animate-spin">sync</span>
        <span>저장 중...</span>
    `;
    saveButton.classList.add('opacity-75', 'cursor-not-allowed');
    
    try {
        const response = await fetch('/api/user/nickname', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nickname: nickname })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showSuccessMessage('닉네임이 변경되었습니다!');
            
            // 사용자 정보 다시 로드
            await loadUserInfo();
            
            // localStorage의 user 정보도 업데이트
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    const userData = JSON.parse(storedUser);
                    userData.nickname = nickname;
                    localStorage.setItem('user', JSON.stringify(userData));
                } catch (e) {
                    console.error('localStorage 업데이트 오류:', e);
                }
            }
        } else {
            alert(result.message || '닉네임 변경에 실패했습니다');
            saveButton.disabled = false;
            saveButton.innerHTML = originalButtonText;
            saveButton.classList.remove('opacity-75', 'cursor-not-allowed');
        }
    } catch (error) {
        console.error('닉네임 변경 오류:', error);
        alert('닉네임 변경 중 오류가 발생했습니다');
        saveButton.disabled = false;
        saveButton.innerHTML = originalButtonText;
        saveButton.classList.remove('opacity-75', 'cursor-not-allowed');
    }
}

// 로그아웃
function handleLogout() {
    if (confirm('로그아웃하시겠습니까?')) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    }
}

// 프로필 이미지 변경
function handleProfileImageChange(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // 파일 크기 확인 (5MB 제한)
    if (file.size > 5 * 1024 * 1024) {
        alert('이미지 크기는 5MB 이하여야 합니다');
        return;
    }
    
    // 이미지 타입 확인
    if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 업로드할 수 있습니다');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = async (e) => {
        const imageDataUrl = e.target.result;
        
        // base64 인코딩된 이미지 URL 길이 확인
        // MySQL TEXT 타입 제한: 65,535 bytes (약 64KB)
        // base64 인코딩 시 원본보다 약 33% 증가하므로 실제로는 약 48KB 원본 이미지만 가능
        // 안전 마진을 고려하여 60,000자로 설정 (약 45KB 원본 이미지)
        const MAX_IMAGE_URL_LENGTH = 60000;
        
        if (imageDataUrl.length > MAX_IMAGE_URL_LENGTH) {
            const urlSizeKB = (imageDataUrl.length / 1024).toFixed(2);
            const maxSizeKB = (MAX_IMAGE_URL_LENGTH / 1024).toFixed(0);
            const originalSizeKB = ((imageDataUrl.length * 0.75) / 1024).toFixed(2);
            
            alert(`⚠️ 이미지 파일이 너무 큽니다!\n\n` +
                  `현재 이미지 크기: ${urlSizeKB}KB (원본 약 ${originalSizeKB}KB)\n` +
                  `최대 허용 크기: ${maxSizeKB}KB (원본 약 ${(MAX_IMAGE_URL_LENGTH * 0.75 / 1024).toFixed(0)}KB)\n\n` +
                  `💡 해결 방법:\n` +
                  `1. 이미지를 압축하거나 크기를 줄여주세요\n` +
                  `2. 더 작은 크기의 이미지를 선택해주세요\n` +
                  `3. 온라인 이미지 압축 도구를 사용하세요`);
            
            // 미리보기 원래대로 되돌리기
            const preview = document.getElementById('profileImagePreview');
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    const userData = JSON.parse(storedUser);
                    if (userData.profileImage && preview) {
                        if (preview.tagName === 'IMG') {
                            preview.src = userData.profileImage;
                        } else {
                            preview.style.backgroundImage = `url('${userData.profileImage}')`;
                        }
                    }
                } catch (e) {
                    console.error('미리보기 복원 오류:', e);
                }
            }
            
            // 파일 입력 초기화
            event.target.value = '';
            return;
        }
        
        // 미리보기 업데이트
        const preview = document.getElementById('profileImagePreview');
        if (preview.tagName === 'IMG') {
            preview.src = imageDataUrl;
        } else {
            // div를 img로 교체
            const img = document.createElement('img');
            img.id = 'profileImagePreview';
            img.src = imageDataUrl;
            img.alt = '프로필';
            img.className = 'w-20 h-20 rounded-full object-cover border-2 border-primary';
            preview.parentNode.replaceChild(img, preview);
        }
        
        // 서버에 업로드
        await updateProfileImage(imageDataUrl);
    };
    reader.readAsDataURL(file);
}

// 프로필 이미지 업데이트
async function updateProfileImage(imageDataUrl) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        return;
    }
    
    try {
        const response = await fetch('/api/user/profile-image', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ profileImage: imageDataUrl })
        });
        
        if (!response.ok) {
            let errorMessage = '프로필 이미지 변경에 실패했습니다';
            let isSizeError = false;
            
            try {
                const errorResult = await response.json();
                if (errorResult.message) {
                    errorMessage = errorResult.message;
                    
                    // 백엔드에서 반환한 에러 메시지를 사용자 친화적으로 변환
                    if (errorMessage.includes('너무 깁니다') || errorMessage.includes('too large') || 
                        errorMessage.includes('Data too long') || errorMessage.includes('truncation')) {
                        isSizeError = true;
                        errorMessage = `⚠️ 이미지 파일이 너무 큽니다!\n\n` +
                                      `데이터베이스에 저장할 수 없는 크기입니다.\n\n` +
                                      `💡 해결 방법:\n` +
                                      `1. 이미지를 압축하거나 크기를 줄여주세요\n` +
                                      `2. 더 작은 크기의 이미지를 선택해주세요 (권장: 50KB 이하)\n` +
                                      `3. 온라인 이미지 압축 도구를 사용하세요\n\n` +
                                      `📝 참고: base64 인코딩으로 인해 원본보다 약 33% 더 큽니다.`;
                    } else if (errorMessage.includes('입력해주세요')) {
                        errorMessage = '이미지 파일을 선택해주세요.';
                    }
                }
            } catch (e) {
                // JSON 파싱 실패 시 상태 코드에 따른 메시지
                if (response.status === 404) {
                    errorMessage = '프로필 이미지 업데이트 API를 찾을 수 없습니다.\n서버를 재시작해주세요.';
                } else if (response.status === 400) {
                    isSizeError = true;
                    errorMessage = `⚠️ 이미지 파일이 너무 크거나 형식이 올바르지 않습니다!\n\n` +
                                  `다음을 확인해주세요:\n` +
                                  `- 이미지 크기: 50KB 이하 (원본 기준)\n` +
                                  `- 이미지 형식: JPG, PNG, GIF 등\n` +
                                  `- 권장 크기: 30KB 이하\n\n` +
                                  `💡 이미지를 압축하거나 더 작은 크기의 이미지를 선택해주세요.`;
                } else if (response.status === 401) {
                    errorMessage = '로그인이 필요합니다.\n다시 로그인해주세요.';
                } else {
                    errorMessage = `프로필 이미지 변경에 실패했습니다.\n(오류 코드: ${response.status})`;
                }
            }
            
            alert(errorMessage);
            
            // 미리보기 원래대로 되돌리기
            const preview = document.getElementById('profileImagePreview');
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    const userData = JSON.parse(storedUser);
                    if (userData.profileImage && preview) {
                        if (preview.tagName === 'IMG') {
                            preview.src = userData.profileImage;
                        } else {
                            preview.style.backgroundImage = `url('${userData.profileImage}')`;
                        }
                    }
                } catch (e) {
                    console.error('미리보기 복원 오류:', e);
                }
            }
            
            // 파일 입력 초기화
            const fileInput = document.getElementById('profileImageInput');
            if (fileInput) {
                fileInput.value = '';
            }
            
            return;
        }
        
        const result = await response.json();
        
        if (result.success) {
            showSuccessMessage('프로필 이미지가 변경되었습니다!');
            
            // localStorage의 user 정보 업데이트
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    const userData = JSON.parse(storedUser);
                    userData.profileImage = imageDataUrl;
                    localStorage.setItem('user', JSON.stringify(userData));
                } catch (e) {
                    console.error('localStorage 업데이트 오류:', e);
                }
            }
            
            // 사용자 정보 다시 로드
            await loadUserInfo();
        } else {
            let errorMessage = result.message || '프로필 이미지 변경에 실패했습니다';
            
            // 에러 메시지를 사용자 친화적으로 변환
            if (errorMessage.includes('너무 깁니다') || errorMessage.includes('too large')) {
                errorMessage = '이미지 파일이 너무 큽니다.\n\n이미지를 압축하거나 더 작은 크기의 이미지를 선택해주세요.\n(권장: 1MB 이하, 최대 5MB)';
            }
            
            alert(errorMessage);
            
            // 미리보기 원래대로 되돌리기
            const preview = document.getElementById('profileImagePreview');
            const storedUser = localStorage.getItem('user');
            if (storedUser) {
                try {
                    const userData = JSON.parse(storedUser);
                    if (userData.profileImage && preview) {
                        if (preview.tagName === 'IMG') {
                            preview.src = userData.profileImage;
                        } else {
                            preview.style.backgroundImage = `url('${userData.profileImage}')`;
                        }
                    }
                } catch (e) {
                    console.error('미리보기 복원 오류:', e);
                }
            }
            
            // 파일 입력 초기화
            const fileInput = document.getElementById('profileImageInput');
            if (fileInput) {
                fileInput.value = '';
            }
        }
    } catch (error) {
        console.error('프로필 이미지 업데이트 오류:', error);
        alert('프로필 이미지 변경 중 오류가 발생했습니다: ' + error.message);
    }
}

// 뒤로가기
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = 'index.html';
    }
}

// 페이지 로드 시 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        loadUserInfo();
        loadPreference();
    });
} else {
    loadUserInfo();
    loadPreference();
}

