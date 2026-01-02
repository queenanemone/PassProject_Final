// 여행 기록 작성
let selectedImages = [];
let currentPlanId = null;

// URL에서 planId 가져오기
const urlParams = new URLSearchParams(window.location.search);
const planIdFromUrl = urlParams.get('planId');
if (planIdFromUrl) {
    currentPlanId = parseInt(planIdFromUrl);
    document.getElementById('planSelect').value = planIdFromUrl;
}

// 여행 계획 목록 로드
async function loadPlans() {
    const token = localStorage.getItem('token');
    if (!token) {
        // 로그인하지 않은 경우 여행 계획 선택 옵션을 비활성화
        const planSelect = document.getElementById('planSelect');
        planSelect.disabled = true;
        planSelect.innerHTML = '<option value="">로그인이 필요합니다</option>';
        return;
    }
    
    try {
        console.log('여행 계획 목록 요청 (기록 페이지)');
        const response = await fetch('/api/plans', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('여행 계획 목록 응답 상태 (기록 페이지):', response.status);
        
        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('여행 계획 목록 응답이 비어있습니다');
            return;
        }
        
        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            return;
        }
        console.log('여행 계획 목록 응답 데이터 (기록 페이지):', result);
        
        if (result.success) {
            const planSelect = document.getElementById('planSelect');
            const plans = result.data || [];
            
            planSelect.innerHTML = '<option value="">선택 안함</option>';
            plans.forEach(plan => {
                const option = document.createElement('option');
                option.value = plan.planId;
                option.textContent = plan.title || `여행 계획 ${plan.planId}`;
                planSelect.appendChild(option);
            });
            
            if (currentPlanId) {
                planSelect.value = currentPlanId;
            }
        }
    } catch (error) {
        console.error('여행 계획 로드 오류:', error);
    }
}

// 이미지 URL 길이 제한 (약 5MB, base64 인코딩 고려)
const MAX_IMAGE_URL_LENGTH = 6500000; // 약 5MB

// 파일 선택 처리
function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    files.forEach(file => {
        if (file.type.startsWith('image/')) {
            // 파일 크기 미리 체크 (원본 파일 크기)
            const maxFileSize = 5000000; // 5MB
            if (file.size > maxFileSize) {
                alert(`이미지 "${file.name}"의 크기가 너무 큽니다 (${(file.size / 1024 / 1024).toFixed(2)}MB).\n\n5MB 이하의 이미지만 업로드할 수 있습니다.`);
                return;
            }
            
            const reader = new FileReader();
            reader.onload = (e) => {
                const imageUrl = e.target.result;
                const urlLength = imageUrl.length;
                
                // base64 URL 길이 체크
                if (urlLength > MAX_IMAGE_URL_LENGTH) {
                    alert(`이미지 "${file.name}"의 URL이 너무 길어서 등록할 수 없습니다 (${(urlLength / 1024 / 1024).toFixed(2)}MB).\n\n더 작은 크기의 이미지를 선택해주세요.`);
                    return;
                }
                
                selectedImages.push({
                    file: file,
                    url: imageUrl,
                    order: selectedImages.length,
                    urlLength: urlLength
                });
                updateImagePreview();
            };
            reader.onerror = () => {
                alert(`이미지 "${file.name}"을 읽는 중 오류가 발생했습니다.`);
            };
            reader.readAsDataURL(file);
        }
    });
}

// 이미지 미리보기 업데이트
function updateImagePreview() {
    const preview = document.getElementById('imagePreview');
    const uploadArea = document.getElementById('imageUploadArea');
    
    if (selectedImages.length > 0) {
        uploadArea.style.display = 'none';
        preview.innerHTML = selectedImages.map((img, index) => {
            const urlLength = img.urlLength || img.url.length;
            const sizeWarning = urlLength > MAX_IMAGE_URL_LENGTH;
            const sizeInfo = (urlLength / 1024 / 1024).toFixed(2);
            
            return `
            <div class="relative group">
                <img src="${img.url}" alt="Preview ${index + 1}" class="w-full h-32 object-cover rounded-lg ${sizeWarning ? 'opacity-50' : ''}"/>
                ${sizeWarning ? `
                    <div class="absolute inset-0 bg-red-900/70 flex items-center justify-center rounded-lg">
                        <div class="text-center text-white p-2">
                            <span class="material-symbols-outlined text-2xl mb-1">warning</span>
                            <p class="text-xs font-bold">URL이 너무 깁니다</p>
                            <p class="text-xs">(${sizeInfo}MB)</p>
                        </div>
                    </div>
                ` : ''}
                <button onclick="removeImage(${index})" class="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <span class="material-symbols-outlined text-sm">close</span>
                </button>
            </div>
        `;
        }).join('');
    } else {
        uploadArea.style.display = 'flex';
        preview.innerHTML = '';
    }
}

function removeImage(index) {
    selectedImages.splice(index, 1);
    updateImagePreview();
}

// 드래그 앤 드롭
const uploadArea = document.getElementById('imageUploadArea');
uploadArea?.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('border-primary');
});

uploadArea?.addEventListener('dragleave', () => {
    uploadArea.classList.remove('border-primary');
});

uploadArea?.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('border-primary');
    const files = Array.from(e.dataTransfer.files);
    files.forEach(file => {
        if (file.type.startsWith('image/')) {
            // 파일 크기 미리 체크
            const maxFileSize = 5000000; // 5MB
            if (file.size > maxFileSize) {
                alert(`이미지 "${file.name}"의 크기가 너무 큽니다 (${(file.size / 1024 / 1024).toFixed(2)}MB).\n\n5MB 이하의 이미지만 업로드할 수 있습니다.`);
                return;
            }
            
            const reader = new FileReader();
            reader.onload = (e) => {
                const imageUrl = e.target.result;
                const urlLength = imageUrl.length;
                
                // base64 URL 길이 체크
                if (urlLength > MAX_IMAGE_URL_LENGTH) {
                    alert(`이미지 "${file.name}"의 URL이 너무 길어서 등록할 수 없습니다 (${(urlLength / 1024 / 1024).toFixed(2)}MB).\n\n더 작은 크기의 이미지를 선택해주세요.`);
                    return;
                }
                
                selectedImages.push({
                    file: file,
                    url: imageUrl,
                    order: selectedImages.length,
                    urlLength: urlLength
                });
                updateImagePreview();
            };
            reader.onerror = () => {
                alert(`이미지 "${file.name}"을 읽는 중 오류가 발생했습니다.`);
            };
            reader.readAsDataURL(file);
        }
    });
});

// 갤러리에서 선택 (한국관광공사 API 연동)
async function selectFromGallery() {
    // TODO: 한국관광공사 관광사진 갤러리 API 연동
    alert('갤러리 기능은 준비 중입니다');
}

// 게시하기
document.getElementById('publishBtn')?.addEventListener('click', async () => {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    const title = document.getElementById('recordTitle').value;
    const content = document.getElementById('recordContent').value;
    const planId = document.getElementById('planSelect').value;
    
    if (!title || !content) {
        alert('제목과 내용을 입력해주세요');
        return;
    }
    
    // 이미지 크기 사전 체크
    if (selectedImages.length > 0) {
        const oversizedImages = selectedImages.filter(img => {
            const urlLength = img.urlLength || img.url.length;
            return urlLength > MAX_IMAGE_URL_LENGTH;
        });
        
        if (oversizedImages.length > 0) {
            const oversizedList = oversizedImages.map((img, idx) => {
                const fileName = img.file ? img.file.name : `이미지 ${idx + 1}`;
                const size = ((img.urlLength || img.url.length) / 1024 / 1024).toFixed(2);
                return `  • ${fileName} (${size}MB)`;
            }).join('\n');
            
            const proceed = confirm(
                `경고: ${oversizedImages.length}개의 이미지가 너무 커서 등록되지 않을 수 있습니다:\n\n${oversizedList}\n\n\n계속하시겠습니까?`
            );
            
            if (!proceed) {
                return;
            }
        }
    }
    
    try {
        
        // 1. 여행 기록 생성
        console.log('게시 요청 시작:', { planId, title });
        const recordResponse = await fetch('/api/records', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                planId: planId && planId !== '' ? parseInt(planId) : null,
                title: title,
                content: content
            })
        });
        console.log('게시 응답 상태:', recordResponse.status);
        
        // 응답이 비어있는지 확인
        const responseText = await recordResponse.text();
        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다');
            alert('서버 응답 오류: 응답이 비어있습니다');
            return;
        }
        
        let recordResult;
        try {
            recordResult = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            alert('서버 응답을 파싱할 수 없습니다: ' + e.message);
            return;
        }
        console.log('게시 결과:', recordResult);
        
        if (!recordResult.success) {
            console.error('게시 실패 상세:', recordResult);
            alert(recordResult.message || '게시에 실패했습니다: ' + JSON.stringify(recordResult));
            return;
        }
        
        if (recordResult.success) {
            const recordId = recordResult.data.recordId;
            
            // 2. 이미지 업로드
            let allImagesUploaded = true;
            if (selectedImages.length > 0) {
                allImagesUploaded = await uploadImages(recordId);
            }
            
            // 3. 게시판에 공유 (선택사항)
            if (confirm('게시판에도 공유하시겠습니까?')) {
                const shared = await shareToBoard(
                    planId && planId !== '' ? parseInt(planId) : null, 
                    title, 
                    content
                );
                if (shared) {
                    alert('여행 기록이 저장되고 게시판에 공유되었습니다!');
                } else {
                    alert('여행 기록은 저장되었지만, 게시판 공유에 실패했습니다.');
                }
            } else {
                alert('여행 기록이 저장되었습니다!');
            }
            
            window.location.href = 'my-records.html';
        } else {
            alert(recordResult.message || '게시에 실패했습니다');
        }
    } catch (error) {
        console.error('게시 오류:', error);
        alert('게시 중 오류가 발생했습니다');
    }
});

// 이미지 업로드
async function uploadImages(recordId) {
    console.log(`이미지 업로드 시작 - recordId: ${recordId}, 이미지 개수: ${selectedImages.length}`);
    
    const failedImages = [];
    
    for (let i = 0; i < selectedImages.length; i++) {
        const img = selectedImages[i];
        const urlLength = img.urlLength || img.url.length;
        const fileName = img.file ? img.file.name : `이미지 ${i + 1}`;
        
        // 이미지 URL 길이 재확인
        if (urlLength > MAX_IMAGE_URL_LENGTH) {
            failedImages.push({
                name: fileName,
                reason: `URL이 너무 깁니다 (${(urlLength / 1024 / 1024).toFixed(2)}MB). 최대 ${(MAX_IMAGE_URL_LENGTH / 1024 / 1024).toFixed(0)}MB까지 가능합니다.`
            });
            console.warn(`이미지 ${i + 1} 스킵: URL이 너무 깁니다 (${urlLength} bytes)`);
            continue;
        }
        
        try {
            const token = localStorage.getItem('token');
            console.log('이미지 업로드 요청:', { recordId, order: i, urlLength: urlLength, fileName });
            
            const response = await fetch('/api/records/images', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    recordId: recordId,
                    imageUrl: img.url,
                    imageOrder: i
                })
            });
            
            // 응답이 비어있는지 확인
            const responseText = await response.text();
            let result;
            if (!responseText || responseText.trim() === '') {
                console.error('이미지 업로드 응답이 비어있습니다');
                failedImages.push({
                    name: fileName,
                    reason: '서버 응답 오류: 응답이 비어있습니다'
                });
                continue;
            }
            
            try {
                result = JSON.parse(responseText);
            } catch (e) {
                console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
                failedImages.push({
                    name: fileName,
                    reason: '서버 응답을 파싱할 수 없습니다'
                });
                continue;
            }
            console.log('이미지 업로드 응답:', { status: response.status, result });
            
            if (!result.success) {
                const errorMessage = result.message || '알 수 없는 오류';
                failedImages.push({
                    name: fileName,
                    reason: errorMessage.includes('too long') || errorMessage.includes('너무') 
                        ? `URL이 너무 길어서 등록할 수 없습니다. 더 작은 크기의 이미지를 선택해주세요.`
                        : errorMessage
                });
                console.error('이미지 업로드 실패:', result);
            } else {
                console.log('이미지 업로드 완료:', { recordId, order: i });
            }
        } catch (error) {
            failedImages.push({
                name: fileName,
                reason: `네트워크 오류: ${error.message}`
            });
            console.error('이미지 업로드 오류:', error);
        }
    }
    
    // 실패한 이미지가 있으면 경고 표시
    if (failedImages.length > 0) {
        const failedList = failedImages.map((img, idx) => 
            `${idx + 1}. ${img.name}: ${img.reason}`
        ).join('\n');
        
        alert(`일부 이미지 업로드에 실패했습니다:\n\n${failedList}\n\n\n나머지 이미지는 정상적으로 저장되었습니다.`);
    }
    
    return failedImages.length === 0;
}

// 게시판에 공유
async function shareToBoard(planId, title, content) {
    try {
        const token = localStorage.getItem('token');
        console.log('게시판 공유 요청:', { planId, title });
        const response = await fetch('/api/board/posts', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                planId: planId,
                title: title,
                content: content,
                regionCode: null,
                tripType: null,
                season: null,
                category: 'TRAVEL_RECORD'
            })
        });
        console.log('게시판 공유 응답 상태:', response.status);
        
        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('게시판 공유 응답이 비어있습니다');
            return false;
        }
        
        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            return false;
        }
        console.log('게시판 공유 결과:', result);
        return result.success;
    } catch (error) {
        console.error('게시판 공유 오류:', error);
        return false;
    }
}

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

// 페이지 로드 시 여행 계획 목록 로드
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        loadPlans();
        updateHeaderForLogin();
    });
} else {
    loadPlans();
    updateHeaderForLogin();
}
