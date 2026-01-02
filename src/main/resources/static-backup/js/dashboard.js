// ===== 무한 리다이렉트 완전 차단 =====
console.log('%c🚀 dashboard.js 로드됨', 'color: green; font-size: 16px; font-weight: bold;');

// 전역 변수
let isLoading = false;
let currentPlanId = null;
let currentPlanData = null; // [추가] 현재 계획의 상세 정보를 저장할 변수

// URL에서 planId 추출
function getPlanIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('planId');
}

// 대시보드 초기화
async function initDashboard() {
    console.log('=== initDashboard 시작 ===');

    currentPlanId = getPlanIdFromUrl();
    console.log('현재 planId:', currentPlanId);

    if (currentPlanId) {
        // planId가 있으면 해당 계획 로드
        await loadPlanDetails(currentPlanId);
    } else {
        // planId가 없으면 계획 목록 표시
        await loadPlanList();
    }
}

// 계획 상세 로드
async function loadPlanDetails(planId) {
    if (isLoading) {
        console.warn('이미 로딩 중입니다');
        return;
    }

    isLoading = true;
    const container = document.getElementById('dashboardContent');

    // 로딩 표시 (기존 내용 유지)
    container.innerHTML = `
        <div class="col-span-full text-center py-20">
            <svg class="animate-spin h-10 w-10 text-primary mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            <p class="text-text-dark-secondary">여행 계획을 불러오는 중...</p>
        </div>
    `;

    try {
        const token = localStorage.getItem('token');

        if (!token) {
            console.error('토큰 없음. 로그인 페이지로 이동');
            window.location.href = 'login.html';
            return;
        }

        console.log(`계획 상세 조회 API 호출: /api/plans/${planId}`);
        const response = await fetch(`/api/plans/${planId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = 'login.html';
            return;
        }

        if (response.status === 404) {
            alert('해당 여행 계획을 찾을 수 없습니다');
            await loadPlanList();
            return;
        }

        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다');
            alert('서버 응답 오류: 응답이 비어있습니다');
            await loadPlanList();
            return;
        }

        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            alert('서버 응답을 파싱할 수 없습니다: ' + e.message);
            await loadPlanList();
            return;
        }

        if (result.success) {
            renderPlanDetails(result.data);
        } else {
            alert(result.message || '데이터를 불러오는데 실패했습니다');
            await loadPlanList();
        }
    } catch (error) {
        console.error('계획 로드 오류:', error);
        alert('서버 오류: ' + error.message);
        await loadPlanList();
    } finally {
        isLoading = false;
    }
}

function renderPlanDetails(data) {
    console.log('=== renderPlanDetails 시작 (Day-by-Day) ===', data);

    // [중요] 현재 계획 정보 저장
    currentPlanData = data.plan;

    const container = document.getElementById('dashboardContent');

    // 1. Day별로 그룹화된 최종 일정 데이터 생성
    const dailyItinerary = organizeItemsByDate(data);

    // 2. 상단 헤더 및 타이틀 섹션
    const headerHtml = `
        <div class="col-span-full mb-4">
            <button onclick="navigateToPlanList()" class="flex items-center gap-2 text-text-dark-secondary hover:text-text-dark-primary transition-colors">
                <span class="material-symbols-outlined">arrow_back</span>
                <span>여행 계획 목록으로</span>
            </button>
        </div>
        <div class="col-span-full flex items-center justify-between bg-card-dark p-6 rounded-2xl border border-white/5 mb-8">
            <div class="flex items-center gap-3">
                <h1 id="planTitleText" class="text-3xl font-bold text-white">${data.plan.title}</h1>
                <button onclick="editTitle(${data.plan.planId})" class="text-gray-400 hover:text-primary transition-colors p-1 rounded-full hover:bg-white/5" title="제목 수정">
                    <span class="material-symbols-outlined text-xl">edit</span>
                </button>
            </div>
            <div class="text-right">
                <p class="text-sm text-gray-400">${data.plan.departureDate} ~ ${data.plan.arrivalDate}</p>
                <p class="text-xs text-gray-500 mt-1">성인 ${data.plan.adultCount}, 아동 ${data.plan.childCount}</p>
            </div>
        </div>
    `;

    // 3. Day별 수직 일정 HTML 생성 (가장 큰 변화)
    const dailyScheduleHtml = dailyItinerary.map(dayData => {
        return createDayCardHtml(dayData); // 신규 렌더링 함수 호출
    }).join('');

    // 4. AI 추천 섹션 (기존 AI 추천은 가장 아래에 별도 섹션으로 유지)
    const aiSection = createAISection('AI 추천', data.aiRecommendations || []);

    // 5. 최종 HTML 조합
    container.innerHTML = headerHtml + `
        <div class="col-span-full flex flex-col gap-10">
            ${dailyScheduleHtml}
            
            <div class="border-t border-border-dark pt-10">
                 ${aiSection}
            </div>
        </div>
    `;

    // AI 추천 버튼 표시 (기존 로직 유지)
    const aiContainer = document.getElementById('aiRecommendContainer');
    if (aiContainer) {
        aiContainer.style.display = 'flex';
    }
}

// 계획 목록 로드
async function loadPlanList() {
    if (isLoading) return;
    isLoading = true;
    const container = document.getElementById('dashboardContent');

    container.innerHTML = `<div class="col-span-full text-center py-10"><p class="text-text-dark-secondary">로딩 중...</p></div>`;

    try {
        const token = localStorage.getItem('token');
        if (!token) { window.location.href = 'login.html'; return; }

        console.log('계획 목록 조회 API 호출: /api/plans');
        const response = await fetch('/api/plans', { headers: { 'Authorization': `Bearer ${token}` } });
        console.log('응답 상태 코드:', response.status);
        console.log('응답 헤더:', Object.fromEntries(response.headers.entries()));

        if (response.status === 401 || response.status === 403) {
            console.error('인증 실패 또는 권한 없음');
            localStorage.removeItem('token'); // 토큰 삭제
            alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) {
            console.error('HTTP 오류:', response.status, response.statusText);
            container.innerHTML = `<div class="col-span-full text-center py-20 text-red-400">서버 오류: ${response.status} ${response.statusText}</div>`;
            return;
        }

        // 응답이 비어있는지 확인
        const responseText = await response.text();
        console.log('응답 텍스트 길이:', responseText ? responseText.length : 0);
        console.log('응답 텍스트 (처음 200자):', responseText ? responseText.substring(0, 200) : 'null');

        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다. 상태 코드:', response.status);
            container.innerHTML = `<div class="col-span-full text-center py-20 text-red-400">서버 응답 오류: 응답이 비어있습니다 (상태 코드: ${response.status})</div>`;
            return;
        }

        let result;
        try {
            result = JSON.parse(responseText);
            console.log('파싱된 결과:', result);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            container.innerHTML = `<div class="col-span-full text-center py-20 text-red-400">서버 응답을 파싱할 수 없습니다: ${e.message}</div>`;
            return;
        }

        if (result.success) {
            renderPlanList(result.data);
        } else {
            container.innerHTML = `<div class="col-span-full text-center py-20 text-red-400">${result.message || '알 수 없는 오류'}</div>`;
        }
    } catch (error) {
        console.error('계획 목록 로드 오류:', error);
        container.innerHTML = `<div class="col-span-full text-center py-20 text-red-400">오류: ${error.message}</div>`;
    } finally {
        isLoading = false;
    }
}

// 계획 목록 렌더링
function renderPlanList(plans) {
    const container = document.getElementById('dashboardContent');

    // 목록 화면에서는 AI 버튼 숨김
    const aiContainer = document.getElementById('aiRecommendContainer');
    if (aiContainer) aiContainer.style.display = 'none';

    if (!plans || plans.length === 0) {
        container.innerHTML = `
            <div class="col-span-full text-center py-20">
                <p class="text-2xl text-text-dark-primary mb-4">여행 계획이 없습니다</p>
                <button onclick="location.href='new-plan.html'" class="px-6 py-3 bg-primary text-white rounded-lg hover:bg-blue-500 transition-colors">새 계획 만들기</button>
            </div>`;
        return;
    }

    const plansHtml = plans.map(plan => `
        <div class="col-span-1">
            <div class="relative group p-6 bg-card-dark rounded-xl hover:ring-2 hover:ring-primary/50 transition-all cursor-pointer" onclick="navigateToPlan(${plan.planId})">
                <button onclick="event.stopPropagation(); deletePlan(${plan.planId})" class="absolute top-4 right-4 p-2 rounded-lg text-gray-500 hover:text-red-500 hover:bg-red-500/10 transition-colors z-10 opacity-100 sm:opacity-0 sm:group-hover:opacity-100" title="계획 삭제">
                    <span class="material-symbols-outlined text-xl">delete</span>
                </button>
                <h3 class="text-xl font-bold text-text-dark-primary mb-2 pr-8">${plan.title || '제목 없음'}</h3>
                <p class="text-sm text-text-dark-secondary mb-1">출발: ${plan.departureDate || '미정'}</p>
                <p class="text-sm text-text-dark-secondary">도착: ${plan.arrivalDate || '미정'}</p>
            </div>
        </div>
    `).join('');

    container.innerHTML = `
        <div class="col-span-full mb-6 flex items-center justify-between">
            <h2 class="text-2xl font-bold text-text-dark-primary">내 여행 계획</h2>
            <button onclick="location.href='new-plan.html'" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-blue-500 transition-colors">새 계획 만들기</button>
        </div>
        ${plansHtml}
    `;
}

// 네비게이션 함수
function navigateToPlan(planId) {
    window.history.pushState({}, '', `dashboard.html?planId=${planId}`);
    currentPlanId = planId;
    loadPlanDetails(planId);
}

function navigateToPlanList() {
    window.history.pushState({}, '', 'dashboard.html');
    currentPlanId = null;
    loadPlanList();
}

function createAISection(title, items) {
    const itemsHtml = items.map(item => {
        // [수정] 데이터 보정: type 속성 강제 주입
        // DB에서 가져올 땐 recommendationType일 수 있으므로 type으로 통일
        item.type = item.type || item.recommendationType || 'destination';

        // 주소나 키워드가 없으면 기본값 채워주기 (UI 오류 방지)
        item.address = item.address || '대한민국 어딘가 (AI 추천)';
        item.image_keyword = item.image_keyword || 'korea';

        // JSON 문자열 생성 (따옴표 이스케이프)
        const itemJson = JSON.stringify(item).replace(/'/g, "&#39;");

        return `
        <div class="flex flex-col gap-3 rounded-xl bg-gradient-to-br from-purple-900/20 to-blue-900/20 p-4 border border-purple-500/30 group hover:border-purple-500/60 transition-all">
            <div class="flex items-start gap-3">
                <span class="material-symbols-outlined text-2xl text-purple-400 mt-1">auto_awesome</span>
                <div class="flex-1">
                    <div class="flex justify-between items-start">
                        <p class="text-base font-bold text-text-dark-primary mb-1">${item.title || '추천 장소'}</p>
                        
                        <button 
                            onclick='addDirectlyFromAI(${itemJson})'
                            class="text-xs bg-purple-500/20 text-purple-300 px-2 py-1 rounded hover:bg-purple-500 hover:text-white transition-colors flex items-center gap-1 shrink-0"
                            title="내 계획에 바로 추가"
                        >
                            <span class="material-symbols-outlined text-sm">add</span> 담기
                        </button>
                    </div>
                    <p class="text-sm text-text-dark-secondary mb-2 line-clamp-3">${item.description || ''}</p>
                    <p class="text-xs text-gray-500 mb-1 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[10px]">location_on</span>
                        ${item.address}
                    </p>
                    ${item.reason ? `<p class="text-xs text-purple-300 italic line-clamp-2">"${item.reason}"</p>` : ''}
                </div>
            </div>
        </div>
    `}).join('');

    const emptyMessage = items.length === 0
        ? '<p class="text-text-dark-secondary">AI 추천 내역이 없습니다</p>'
        : '';

    return `
        <div class="flex-1 flex flex-col gap-4 w-full min-w-[300px]">
            <h2 class="text-xl font-bold text-text-dark-primary px-2 flex items-center gap-2">
                ${title} <span class="text-xs font-normal text-purple-400 bg-purple-900/30 px-2 py-0.5 rounded-full">AI</span>
            </h2>
            <div class="grid grid-cols-1 gap-4 w-full">
                ${itemsHtml || emptyMessage}
            </div>
        </div>
    `;
}
// ===== [추가] AI로 전체 채우기 관련 로직 =====

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
                // 프로필 이미지가 있으면 이미지 표시하고 아이콘 완전히 제거
                mypageIcon.style.backgroundImage = `url('${userData.profileImage}')`;
                mypageIcon.style.backgroundSize = 'cover';
                mypageIcon.style.backgroundPosition = 'center';
                mypageIcon.style.backgroundRepeat = 'no-repeat';
                mypageIcon.innerHTML = ''; // 아이콘 완전히 제거
            } else {
                // 프로필 이미지가 없으면 기본 배경색과 아이콘 표시
                mypageIcon.style.backgroundImage = 'none';
                mypageIcon.style.backgroundColor = '';
                mypageIcon.innerHTML = '<span class="material-symbols-outlined">person</span>';
            }
        } catch (e) {
            console.error('사용자 정보 파싱 오류:', e);
        }
    }
}

// ===== 페이지 로드 시 초기화 =====
document.addEventListener('DOMContentLoaded', () => {
    console.log('%c✅ DOMContentLoaded 이벤트 발생', 'color: cyan; font-size: 14px;');
    updateHeaderForLogin();
    initDashboard();

    // AI 채우기 버튼 리스너 등록
    const fillBtn = document.getElementById('fillWithAiBtn');
    if (fillBtn) {
        fillBtn.addEventListener('click', handleAiFillClick);
    }

    // 모달 닫기 버튼 리스너
    document.getElementById('closeAiModal')?.addEventListener('click', () => {
        document.getElementById('aiRecommendModal').classList.add('hidden');
    });

    // 1. 관광지/숙소 검색창 (searchKeyword)
    const keywordInput = document.getElementById('searchKeyword');
    if (keywordInput) {
        keywordInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                // 현재 탭(currentSearchTab) 상태에 맞춰 알아서 검색됨
                executeSearch('tour');
            }
        });
    }

    // 2. 기차 검색창 (출발역/도착역)
    const trainInputs = ['trainDep', 'trainArr'];
    trainInputs.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    executeSearch('train');
                }
            });
        }
    });
});

// 1. AI 버튼 클릭 핸들러
function handleAiFillClick() {
    if (!currentPlanId) return;

    // (A) 정보가 이미 있음 (목적지 코드가 존재) -> 바로 실행
    if (currentPlanData && currentPlanData.arrivalRegionCode) {
        if (!confirm("AI가 이 여행 계획을 자동으로 채워줍니다.\n기존 데이터는 유지되거나 업데이트됩니다. 계속하시겠습니까?")) return;
        executeFillPlan(currentPlanId, null);
    }
    // (B) 정보가 없음 (빈 계획) -> 위치 기반 추천 실행
    else {
        startLocationBasedRecommendation(currentPlanId);
    }
}

// 2. 위치 기반 추천 시작
function startLocationBasedRecommendation(planId) {
    const container = document.getElementById('dashboardContent');
    // 로딩 표시
    container.innerHTML = `
        <div class="py-40 text-center flex flex-col items-center gap-4">
            <svg class="animate-spin h-10 w-10 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            <p class="text-white text-lg">현재 위치를 확인하고 갈만한 곳을 찾는 중입니다...</p>
        </div>
    `;

    // 위치 정보 요청
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                fetchAndShowRecommendations(planId, latitude, longitude);
            },
            (error) => {
                console.warn("위치 정보 실패/거부:", error);
                // 실패 시 서울 기준
                fetchAndShowRecommendations(planId, 37.5665, 126.9780);
            }
        );
    } else {
        alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
        fetchAndShowRecommendations(planId, 37.5665, 126.9780);
    }
}

// 3. 추천 목록 가져오기 및 표시
async function fetchAndShowRecommendations(planId, lat, lon) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/recommend-destinations?lat=${lat}&lon=${lon}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다');
            throw new Error('서버 응답 오류: 응답이 비어있습니다');
        }

        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            throw new Error('서버 응답을 파싱할 수 없습니다: ' + e.message);
        }

        if (!result.success) throw new Error(result.message);

        // 모달에 내용 채우기
        const resultsContainer = document.getElementById('aiRecommendResults');
        resultsContainer.innerHTML = ''; // 초기화

        result.data.forEach(rec => {
            const card = document.createElement('div');
            card.className = "flex flex-col gap-2 p-4 rounded-lg bg-background-dark border border-white/10 hover:border-primary cursor-pointer transition-colors";

            card.innerHTML = `
                <div class="flex justify-between items-center">
                    <h3 class="text-lg font-bold text-white">${rec.regionName}</h3>
                    <span class="text-xs bg-primary/20 text-primary px-2 py-1 rounded">추천</span>
                </div>
                <p class="text-sm text-gray-400">${rec.reason}</p>
                <button class="mt-2 w-full py-2 rounded bg-white/5 hover:bg-primary hover:text-white text-sm transition-colors text-text-dark-secondary">
                    이곳으로 결정하기
                </button>
            `;

            // 카드 클릭 시 선택
            card.onclick = () => selectRecommendation(planId, rec);
            resultsContainer.appendChild(card);
        });

        // 대시보드 배경 복구 (목록이 아니라 빈 화면이라도 기본 틀 유지)
        document.getElementById('dashboardContent').innerHTML = `
            <div class="col-span-full text-center py-20">
                <p class="text-xl text-white">여행지를 선택해주세요</p>
            </div>
        `;

        // 모달 표시
        document.getElementById('aiRecommendModal').classList.remove('hidden');
        document.getElementById('aiRecommendModal').classList.add('flex');

    } catch (e) {
        console.error(e);
        alert("추천 정보를 가져오는데 실패했습니다.");
        initDashboard(); // 리셋
    }
}

// 4. 추천 선택 후 실행
async function selectRecommendation(planId, rec) {
    // 모달 닫기
    document.getElementById('aiRecommendModal').classList.add('hidden');
    document.getElementById('aiRecommendModal').classList.remove('flex');

    // 날짜 자동 설정 (내일 ~ 모레)
    const today = new Date();
    const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
    const dayAfter = new Date(today); dayAfter.setDate(today.getDate() + 2);

    const formatDate = (d) => d.toISOString().split('T')[0];

    const updateData = {
        arrivalRegionCode: rec.regionCode,
        departureRegionCode: "1", // 출발지는 임시로 서울(1)
        departureDate: formatDate(tomorrow),
        arrivalDate: formatDate(dayAfter),
        adultCount: 1,
        hasPet: false
    };

    await executeFillPlan(planId, updateData);
}

// 5. 실제 채우기 실행 API (공통)
async function executeFillPlan(planId, updateData) {
    const container = document.getElementById('dashboardContent');

    // 로딩 UI
    container.innerHTML = `
        <div class="col-span-full flex flex-col items-center justify-center py-40 gap-4">
            <svg class="animate-spin h-12 w-12 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            <p class="text-xl font-bold text-white">AI가 여행 계획을 만들고 있습니다...</p>
            <p class="text-sm text-gray-400">관광지, 맛집, 교통편을 모두 분석 중입니다.</p>
        </div>
    `;

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${planId}/fill`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData || {})
        });

        if (response.ok) {
            alert("여행 계획이 완성되었습니다!");
            window.location.reload();
        } else {
            throw new Error(await response.text());
        }
    } catch (error) {
        alert("실패: " + error.message);
        window.location.reload();
    }
}

// ===== 삭제 함수 =====
async function deletePlan(planId) {
    if (!planId) return;
    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${planId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            await loadPlanList();
        } else {
            alert('삭제 실패');
        }
    } catch (error) {
        alert("오류 발생");
    }
}

window.addEventListener('popstate', () => initDashboard());

// 제목 수정 함수
async function editTitle(planId) {
    const currentTitle = document.getElementById('planTitleText').innerText;
    const newTitle = prompt("수정할 여행 제목을 입력하세요:", currentTitle);

    if (newTitle === null) return; // 취소 누름
    if (newTitle.trim() === "") {
        alert("제목을 입력해주세요.");
        return;
    }

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${planId}/title`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ title: newTitle })
        });

        if (response.ok) {
            // UI 즉시 업데이트 (새로고침 없이)
            document.getElementById('planTitleText').innerText = newTitle;
            // 전역 데이터도 갱신
            if (currentPlanData) currentPlanData.title = newTitle;
        } else {
            alert("제목 수정 실패");
        }
    } catch (error) {
        console.error(error);
        alert("오류가 발생했습니다.");
    }
}

// ===== 개별 아이템 삭제 함수 =====
async function deleteItem(category, itemId, event) {
    // 버블링 방지 (카드 클릭 이벤트 등이 있다면 막아줌)
    if (event) event.stopPropagation();

    if (!confirm("이 항목을 삭제하시겠습니까?")) return;

    // API 엔드포인트 매핑
    let endpoint = "";
    if (category === 'destination') endpoint = `/api/plans/destinations/${itemId}`;
    else if (category === 'accommodation') endpoint = `/api/plans/accommodations/${itemId}`;
    else if (category === 'transportation') endpoint = `/api/plans/transportations/${itemId}`;
    else {
        console.error("알 수 없는 카테고리:", category);
        return;
    }

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            // 성공 시 현재 보고 있는 계획 다시 로드 (화면 갱신)
            if (currentPlanId) {
                await loadPlanDetails(currentPlanId);
            }
        } else {
            const errorText = await response.text();
            alert("삭제 실패: " + errorText);
        }
    } catch (error) {
        console.error("삭제 오류:", error);
        alert("서버 통신 오류가 발생했습니다.");
    }
}

// ===== 수동 추가 시스템 =====

let currentSearchTab = 'tour'; // 'tour' | 'accom' | 'train'

// 1. 모달 열기/닫기
function openSearchModal(initialTab = 'tour') {
    document.getElementById('unifiedSearchModal').classList.remove('hidden');

    // 초기 탭 설정
    switchSearchTab(initialTab); // [수정] 모달을 열면서 탭을 초기화합니다.

    // 현재 계획의 정보를 바탕으로 기본값 세팅 (편의성)
    if (currentPlanData) {
        // 지역 코드 세팅
        const areaSelect = document.getElementById('searchAreaCode');
        if (currentPlanData.arrivalRegionCode) areaSelect.value = currentPlanData.arrivalRegionCode;

        // 기차역 세팅 (간단 매핑)
        const regionToStation = { "1": "서울", "6": "부산", "2": "인천", "4": "동대구", "3": "대전", "5": "광주송정" };
        if (currentPlanData.departureRegionCode) document.getElementById('trainDep').value = regionToStation[currentPlanData.departureRegionCode] || "";
        if (currentPlanData.arrivalRegionCode) document.getElementById('trainArr').value = regionToStation[currentPlanData.arrivalRegionCode] || "";
    }
}

function closeSearchModal() {
    document.getElementById('unifiedSearchModal').classList.add('hidden');
}

// 2. 탭 전환 (기존 switchSearchTab 함수와 동일)
function switchSearchTab(tab) {
    currentSearchTab = tab;

    // 탭 스타일 변경
    ['tour', 'accom', 'train'].forEach(t => {
        const btn = document.getElementById(`tab-${t}`);
        if (!btn) return; // 버튼이 없을 경우 대비

        if (t === tab) {
            btn.className = "flex-1 py-2 rounded-md text-sm font-medium transition-colors bg-primary text-white shadow-md";
        } else {
            btn.className = "flex-1 py-2 rounded-md text-sm font-medium transition-colors text-gray-400 hover:text-white hover:bg-white/5";
        }
    });

    // 폼 전환
    const tourForm = document.getElementById('form-tour');
    const trainForm = document.getElementById('form-train');

    if (tab === 'train') {
        tourForm.classList.add('hidden');
        trainForm.classList.remove('hidden');
    } else {
        tourForm.classList.remove('hidden');
        trainForm.classList.add('hidden');

        // 관광지 vs 숙소 힌트 변경
        document.getElementById('searchKeyword').placeholder =
            tab === 'tour' ? "관광지명 (예: 해운대)" : "숙소명 (예: 신라스테이)";
    }

    // 결과 초기화
    document.getElementById('searchResults').innerHTML = '<p class="text-center text-gray-500 mt-10">검색 조건을 입력하고 검색하세요.</p>';
}

// 3. 검색 실행
async function executeSearch(type) {
    const list = document.getElementById('searchResults');
    list.innerHTML = '<div class="text-center py-10"><svg class="animate-spin h-8 w-8 text-primary mx-auto" ...></svg></div>'; // 로딩 스피너(생략)

    try {
        const token = localStorage.getItem('token');
        let url = '';

        if (type === 'train') {
            const dep = document.getElementById('trainDep').value;
            const arr = document.getElementById('trainArr').value;
            const time = document.getElementById('trainTime').value;
            const tType = document.getElementById('trainType').value;
            // 날짜는 현재 계획의 출발일 사용
            const date = currentPlanData ? currentPlanData.departureDate : new Date().toISOString().split('T')[0];

            url = `/api/plans/search/train?depStation=${dep}&arrStation=${arr}&date=${date}&time=${time}&trainType=${tType}`;
        } else {
            // 관광지 or 숙소
            const keyword = document.getElementById('searchKeyword').value;
            const area = document.getElementById('searchAreaCode').value;
            const contentTypeId = currentSearchTab === 'accom' ? '32' : '12';

            url = `/api/plans/search/tour?keyword=${keyword}&contentTypeId=${contentTypeId}&areaCode=${area}`;
        }

        const response = await fetch(url, { headers: { 'Authorization': `Bearer ${token}` } });

        // 응답이 비어있는지 확인
        const responseText = await response.text();
        if (!responseText || responseText.trim() === '') {
            console.error('서버 응답이 비어있습니다');
            list.innerHTML = `<p class="text-center text-red-400 py-10">서버 응답 오류: 응답이 비어있습니다</p>`;
            return;
        }

        let result;
        try {
            result = JSON.parse(responseText);
        } catch (e) {
            console.error('JSON 파싱 오류:', e, '응답 내용:', responseText);
            list.innerHTML = `<p class="text-center text-red-400 py-10">서버 응답을 파싱할 수 없습니다: ${e.message}</p>`;
            return;
        }

        if (result.success) {
            renderSearchResults(result.data);
        } else {
            list.innerHTML = `<p class="text-center text-red-400 py-10">${result.message}</p>`;
        }
    } catch (e) {
        list.innerHTML = `<p class="text-center text-red-400 py-10">오류 발생</p>`;
    }
}

// 4. 결과 렌더링 (원래대로 심플하게 복구)
function renderSearchResults(items) {
    const list = document.getElementById('searchResults');
    list.innerHTML = '';

    if (!items || items.length === 0) {
        list.innerHTML = '<p class="text-center text-gray-500 py-10">검색 결과가 없습니다.</p>';
        return;
    }

    items.forEach(item => {
        const el = document.createElement('div');
        // 기존의 컴팩트한 스타일
        el.className = "flex items-center gap-4 bg-white/5 p-3 rounded-lg border border-white/5 hover:border-primary transition-colors";

        let contentHtml = '';

        if (item.type === 'train') {
            // [복구] 기차 정보 심플 버전
            contentHtml = `
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                        <span class="font-bold text-primary">${item.trainType}</span>
                        <span class="text-white text-sm">${item.trainNo}</span>
                    </div>
                    <div class="text-sm text-gray-300">
                        ${item.departureTime} ${item.departureStation} → ${item.arrivalTime} ${item.arrivalStation}
                    </div>
                    <div class="text-sm text-green-400 mt-1">${Number(item.fare).toLocaleString()}원</div>
                </div>
            `;
        } else {
            // 관광지/숙소 (기존 동일)
            const img = item.image || 'https://via.placeholder.com/150?text=No+Image';
            contentHtml = `
                <img src="${img}" class="w-16 h-16 rounded object-cover bg-gray-700">
                <div class="flex-1">
                    <h4 class="font-bold text-white text-sm line-clamp-1">${item.title}</h4>
                    <p class="text-xs text-gray-400 line-clamp-2">${item.addr || ''}</p>
                </div>
            `;
        }

        el.innerHTML = `
            ${contentHtml}
            <button onclick='addItemToPlan(${JSON.stringify(item).replace(/'/g, "&#39;")})' class="p-2 bg-primary/20 text-primary rounded hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined text-lg">add</span>
            </button>
        `;
        list.appendChild(el);
    });
}

// 5. 항목 추가
async function addItemToPlan(item) {
    if (!confirm("이 항목을 계획에 추가하시겠습니까?")) return;

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${currentPlanId}/add-item`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(item)
        });

        if (response.ok) {
            alert("추가되었습니다!");
            closeSearchModal();
            loadPlanDetails(currentPlanId); // 화면 갱신
        } else {
            alert("추가 실패");
        }
    } catch (e) {
        alert("오류 발생");
    }
}

async function addDirectlyFromAI(itemData) {
    if (!confirm(`'${itemData.title}' 항목을 계획에 바로 추가하시겠습니까?`)) return;

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${currentPlanId}/ai-recommendations/direct-add`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(itemData)
        });

        if (response.ok) {
            alert("추가되었습니다!");
            loadPlanDetails(currentPlanId); // 화면 갱신 (관광지/숙소 섹션으로 이동됨)
        } else {
            alert("추가 실패: " + await response.text());
        }
    } catch (e) {
        console.error(e);
        alert("오류 발생");
    }
}

// dashboard.js 내부 (기존 organizeItemsByDate 함수 전체 대체)

/**
 * 백엔드에서 받은 flat data (관광지, 숙소, 교통)를 날짜별 그룹으로 재구성하고 시간순으로 정렬합니다.
 * @param {Object} data - API 응답 데이터 (plan, destinationDetails, accommodations, transportations 포함)
 * @returns {Array<Object>} - 날짜별로 시간순 정렬된 Day Plan 배열
 */
function organizeItemsByDate(data) {
    const plan = data.plan;

    // 날짜가 없으면 빈 배열 반환
    if (!plan.departureDate || !plan.arrivalDate) return [];

    const startDate = new Date(plan.departureDate);
    const endDate = new Date(plan.arrivalDate);

    // Day별 배열 초기화
    const dayMap = new Map();
    let currentDate = new Date(startDate);
    let dayNum = 1;

    // Day Map을 날짜 문자열(YYYY-MM-DD)로 채웁니다.
    while (currentDate <= endDate) {
        const dateStr = currentDate.toISOString().split('T')[0];
        dayMap.set(dateStr, {
            dayNum: dayNum++,
            dateStr: dateStr,
            items: []
        });
        currentDate.setDate(currentDate.getDate() + 1);
    }

    // Helper: 항목을 해당 날짜 그룹에 추가하고 정렬 키를 부여
    // sortKey는 YYYY-MM-DDTtt:tt:tt 형식의 문자열이 됩니다.
    const addItem = (dateStr, type, data, sortKey) => {
        const day = dayMap.get(dateStr);
        if (day) {
            day.items.push({ type, data, sortKey });
        }
    };

    // ----------------------------------------------------
    // 2. 항목들을 날짜 기반으로 그룹화 (정렬 키 부여)
    // ----------------------------------------------------

    // 2-1. 관광지 (destinationDetails) 그룹화
    (data.destinationDetails || []).forEach(d => {
        // d.planDestination.visitDate: YYYY-MM-DD 또는 YYYY-MM-DDTtt:tt:tt
        const visitDateTimeStr = d.planDestination.visitDate;
        if (visitDateTimeStr) {
            const datePart = visitDateTimeStr.toString().split('T')[0];

            // 시간 정보가 없으면 기본값 (오전 10시)을 부여하여 정렬에 사용합니다.
            const sortKey = visitDateTimeStr.toString().includes('T')
                ? visitDateTimeStr
                : `${datePart}T10:00:00`;

            addItem(datePart, 'attraction', d, sortKey);
        }
    });

    // 2-2. 숙소 (accommodations) 그룹화
    (data.accommodations || []).forEach(a => {
        const checkInDateStr = a.checkInDate; // YYYY-MM-DD
        if (checkInDateStr) {
            const datePart = checkInDateStr.toString().split('T')[0];
            // 숙소는 일반적으로 체크인 시간(15시)으로 고정하여 정렬 키 부여
            const sortKey = `${datePart}T15:00:00`;

            addItem(datePart, 'hotel', a, sortKey);
        }
    });

    // 2-3. 교통 (transportations) 그룹화
    (data.transportations || []).forEach(t => {
        const departureTimeStr = t.departureTime; // YYYY-MM-DDTtt:tt:tt
        if (departureTimeStr) {
            const datePart = departureTimeStr.toString().split('T')[0];
            // 교통은 출발 시간을 가장 정확한 정렬 키로 사용
            const sortKey = departureTimeStr;

            addItem(datePart, 'transport', t, sortKey);
        }
    });

    // ----------------------------------------------------
    // 3. 날짜별 최종 정렬 및 반환
    // ----------------------------------------------------
    const finalItinerary = Array.from(dayMap.values());

    // 각 날짜별로 항목 순서 정렬 (sortKey 문자열을 기준으로 시간순 정렬)
    finalItinerary.forEach(day => {
        day.items.sort((a, b) => {
            // YYYY-MM-DDTtt:tt:tt 형식의 문자열은 사전순 정렬이 시간순 정렬과 동일합니다.
            if (a.sortKey < b.sortKey) return -1;
            if (a.sortKey > b.sortKey) return 1;

            // 시간이 같으면 항목 유형(attraction, hotel, transport) 순으로 정렬 (보조 키)
            if (a.type < b.type) return -1;
            if (a.type > b.type) return 1;

            return 0;
        });
    });

    return finalItinerary;
}

// dashboard.js 내부 (새로 추가)

/**
 * 날짜별 계획 데이터를 받아 수직으로 배열될 HTML 카드 블록을 생성합니다.
 * @param {Object} dayData - 해당 날짜의 계획 데이터 (dayNum, dateStr, items)
 * @returns {string} - 생성된 HTML 문자열
 */
function createDayCardHtml(dayData) {
    const dayOfWeek = new Date(dayData.dateStr).toLocaleDateString('ko-KR', { weekday: 'short' });

    // 1. Day 내부 아이템 HTML 생성
    let itemsHtml = '';

    if (dayData.items.length === 0) {
        itemsHtml = `<div class="text-center py-6 text-gray-600 dark:text-gray-500 text-sm border-2 border-dashed border-gray-700 dark:border-border-dark rounded-lg">해당 일자에 예정된 일정이 없습니다.</div>`;
    } else {
        itemsHtml = dayData.items.map(item => {
            const type = item.type;
            const itemData = item.data;

            // 기존 createSection 로직의 디자인을 재활용

            // 1) ID 추출 및 삭제 버튼 로직
            let itemId = itemData.planDestination?.planDestinationId || itemData.planAccommodationId || itemData.transportationId;
            const category = type === 'attraction' ? 'destination' : type === 'hotel' ? 'accommodation' : 'transportation';

            const deleteButton = itemId ? `
                <button onclick="deleteItem('${category}', '${itemId}', event)" class="absolute top-2 right-2 p-1.5 rounded-full bg-black/50 text-white/70 hover:bg-red-500 hover:text-white transition-all opacity-0 group-hover:opacity-100"><span class="material-symbols-outlined text-sm">close</span></button>
            ` : '';

            // 2) 교통편 (교통편은 TransportationInfo 엔티티의 최신 디자인을 사용)
            if (type === 'transport') {
                const t = itemData;
                const depTime = t.departureTime ? t.departureTime.split('T')[1].substring(0, 5) : '';
                const arrTime = t.arrivalTime ? t.arrivalTime.split('T')[1].substring(0, 5) : '';
                const price = t.price && t.price > 0 ? `<span class="text-green-400 font-bold text-sm">${t.price.toLocaleString()}원</span>` : `<span class="text-gray-500 text-xs">가격 정보 없음</span>`;
                const colorClass = t.transportType?.includes('SRT') ? 'from-red-900/40 to-red-700/40 border-red-400/50' : 'from-blue-900/40 to-blue-700/40 border-blue-400/50';
                const icon = t.transportType?.includes('버스') ? 'directions_bus' : 'train';

                return `
                    <div class="group relative col-span-1">
                        <div class="relative flex flex-col gap-3 rounded-xl bg-card-dark p-4 transition-all hover:ring-2 hover:ring-primary/80">
                            ${deleteButton}
                            <div class="flex items-center gap-4 p-4 bg-gradient-to-r ${colorClass} rounded-lg border">
                                <span class="material-symbols-outlined text-4xl text-blue-300 self-start mt-1">${icon}</span>
                                <div class="flex-1 flex flex-col gap-1">
                                    <div class="flex justify-between items-center">
                                        <span class="text-base font-bold text-text-dark-primary">${t.transportType || '교통수단'}</span>
                                        ${price}
                                    </div>
                                    <div class="flex items-center gap-2 my-1">
                                        <span class="text-xl font-bold text-white tracking-wide">${depTime}</span>
                                        <span class="material-symbols-outlined text-gray-400 text-sm">arrow_forward</span>
                                        <span class="text-xl font-bold text-white tracking-wide">${arrTime}</span>
                                    </div>
                                    <div class="flex items-center gap-2 text-xs text-text-dark-secondary">
                                        <span>${t.departureLocation || '출발'}</span>
                                        <span class="w-1 h-1 rounded-full bg-gray-600"></span>
                                        <span>${t.arrivalLocation || '도착'}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>`;

            } else {
                // 3) 관광지/숙소 (TourApiCache와 조인된 항목)
                const tourInfo = itemData.tourInfo;
                const title = tourInfo.title || '정보 없음';
                const addr = tourInfo.addr1 || '';
                const imageUrl = tourInfo.firstImage || tourInfo.firstImage2;

                return `
                    <div class="group relative col-span-1">
                        <div class="flex flex-col gap-3 rounded-xl bg-card-dark p-4 transition-all hover:ring-2 hover:ring-primary/80 overflow-hidden">
                            ${deleteButton}
                            <div class="aspect-video w-full overflow-hidden rounded-lg bg-gray-700 relative">
                                ${imageUrl ? `<img src="${imageUrl}" alt="${title}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110">` : `<div class="flex items-center justify-center h-full bg-gray-700"><span class="material-symbols-outlined text-6xl text-gray-500">image</span></div>`}
                            </div>
                            <div class="flex flex-col gap-1">
                                <p class="text-base font-bold text-text-dark-primary line-clamp-1 group-hover:text-primary transition-colors">${title}</p>
                                <p class="text-xs text-text-dark-secondary line-clamp-1">${addr}</p>
                                ${type === 'hotel' ? '<span class="text-xs text-green-400 font-medium">🏨 숙소 체크인 예정</span>' : ''}
                            </div>
                        </div>
                    </div>`;
            }
        }).join('');
    }

    // 2. 전체 Day 카드 구조 (수직 배열)
    return `
        <div class="w-full mb-8">
            <div class="flex items-center justify-between p-4 mb-6 rounded-xl bg-card-dark border border-border-dark shadow-xl">
                <h2 class="text-2xl font-bold text-white">Day ${dayData.dayNum}: ${dayData.dateStr} (${dayOfWeek})</h2>
            </div>
            
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                ${itemsHtml}
            </div>
        </div>
    `;
}
// dashboard.js (saveNewOrder 함수)

async function saveNewOrder(dailyItinerary) {
    const payload = {
        itinerary: dailyItinerary.map(day => ({
            dateStr: day.dateStr,
            items: day.items.map(item => ({
                itemId: item.data.planDestinationId || item.data.planAccommodationId || item.data.transportationId,
                category: item.type === 'attraction' ? 'destination' : item.type === 'hotel' ? 'accommodation' : 'transportation',
            }))
        }))
    };

    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/plans/${currentPlanId}/reorder`, {
            method: 'PUT',
            headers: { /* ... headers ... */ },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('순서 저장 실패');

        console.log("새로운 일정 순서가 저장되었습니다.");
        // 저장 후 화면을 다시 로드하여 최신 순서를 반영 (선택사항)
        // loadPlanDetails(currentPlanId); 

    } catch (error) {
        console.error("순서 저장 오류:", error);
        alert("일정 순서를 저장하는 데 실패했습니다.");
    }
}