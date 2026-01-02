document.addEventListener('DOMContentLoaded', () => {
    console.log("Plan.js loaded");

    // 1. 인원 수 조절 로직
    let adultCount = 2;
    const adultInput = document.getElementById('adultCount');

    document.getElementById('increaseAdult')?.addEventListener('click', () => {
        adultCount++;
        if (adultInput) adultInput.value = adultCount;
    });

    document.getElementById('decreaseAdult')?.addEventListener('click', () => {
        if (adultCount > 1) {
            adultCount--;
            if (adultInput) adultInput.value = adultCount;
        }
    });

    // 2. [핵심 수정] 'AI 추천으로 계획 생성' 버튼 클릭 이벤트
    // 'submit'이 아니라 'click' 이벤트를 사용합니다.
    const createBtn = document.getElementById('createPlanBtn');

    if (createBtn) {
        createBtn.addEventListener('click', async () => {
            console.log("생성 버튼 클릭됨");

            // --- 데이터 수집 ---
            const arrivalRegion = document.getElementById('arrivalRegion');
            const departureRegion = document.getElementById('departureRegion');
            const departureDate = document.getElementById('departureDate');
            const arrivalDate = document.getElementById('arrivalDate');
            const hasPet = document.getElementById('hasPet');
            const titleInput = document.getElementById('planTitle');
            const userTitle = titleInput ? titleInput.value.trim() : '';

            const planData = {
                title: userTitle || "새 여행 계획",
                arrivalRegionCode: arrivalRegion ? arrivalRegion.value : null,
                departureRegionCode: departureRegion ? departureRegion.value : null,
                departureDate: departureDate ? departureDate.value : null,
                arrivalDate: arrivalDate ? arrivalDate.value : null,
                adultCount: parseInt(adultInput ? adultInput.value : 2),
                hasPet: hasPet ? hasPet.checked : false
            };

            // --- 유효성 검사 ---
            if (!planData.arrivalRegionCode || !planData.departureRegionCode || !planData.departureDate || !planData.arrivalDate) {
                alert('출발지, 도착지, 여행 기간을 모두 입력해주세요');
                return;
            }

            if (planData.arrivalRegionCode === planData.departureRegionCode) {
                alert('출발지와 도착지는 같을 수 없습니다.');
                return;
            }

            // --- 로딩창 표시 함수 ---
            const showLoading = () => {
                const overlay = document.getElementById('loadingOverlay');
                if (overlay) {
                    overlay.classList.remove('hidden');

                    const messages = [
                        "🚅 기차표 정보를 조회하고 있습니다...",
                        "🏨 추천 숙소를 검색 중입니다...",
                        "📸 인기 관광지 데이터를 분석합니다...",
                        "✨ AI가 최적의 코스를 만드는 중입니다..."
                    ];
                    let i = 0;
                    const msgEl = document.getElementById('loadingMessage');

                    if (msgEl) {
                        // 기존 인터벌 제거 (중복 방지)
                        if (window.loadingInterval) clearInterval(window.loadingInterval);

                        window.loadingInterval = setInterval(() => {
                            if (i < messages.length) {
                                msgEl.innerText = messages[i++];
                            }
                        }, 1500);
                    }
                }
            };

            // --- API 호출 ---
            try {
                showLoading(); // 로딩 시작

                const token = localStorage.getItem('token');
                console.log('API 요청 데이터:', planData);

                const response = await fetch('/api/plans', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(planData)
                });

                console.log('응답 상태:', response.status);

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`Error: ${response.status} - ${errorText}`);
                }

                const result = await response.json();

                if (result.success) {
                    // 성공 시 페이지 이동
                    window.location.href = `dashboard.html?planId=${result.data.planId}`;
                } else {
                    throw new Error(result.message || '생성 실패');
                }

            } catch (error) {
                console.error('생성 오류:', error);
                alert('오류가 발생했습니다: ' + error.message);

                // 에러 발생 시 로딩창 끄기
                document.getElementById('loadingOverlay')?.classList.add('hidden');
                if (window.loadingInterval) clearInterval(window.loadingInterval);
            }
        });
    } else {
        console.error("오류: HTML에 'createPlanBtn' 아이디를 가진 버튼이 없습니다.");
    }

    // 3. '건너뛰고 빈 계획 만들기' 버튼
    const skipBtn = document.getElementById('skipBtn');
    if (skipBtn) {
        skipBtn.addEventListener('click', async () => {
            try {
                const token = localStorage.getItem('token');
                const response = await fetch('/api/plans', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({}) // 빈 객체 전송
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        window.location.href = `dashboard.html?planId=${result.data.planId}`;
                    }
                }
            } catch (error) {
                console.error(error);
                alert('빈 계획 생성 실패');
            }
        });
    }
});