// 여행 계획 공유하기
async function loadPlans() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    try {
        console.log('공유페이지: 여행 계획 목록 요청');
        const response = await fetch('/api/plans', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('공유페이지: 여행 계획 목록 응답 상태:', response.status);
        const result = await response.json();
        console.log('공유페이지: 여행 계획 목록 결과:', result);
        
        if (result.success) {
            const planSelect = document.getElementById('planSelect');
            const plans = result.data || [];
            
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

document.getElementById('shareBtn')?.addEventListener('click', async () => {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인이 필요합니다');
        window.location.href = 'login.html';
        return;
    }
    
    const planId = document.getElementById('planSelect').value;
    const title = document.getElementById('postTitle').value;
    const content = document.getElementById('postContent').value;
    const regionCode = document.getElementById('regionCode').value;
    const tripType = document.getElementById('tripType').value;
    const season = document.getElementById('season').value;
    
    if (!planId) {
        alert('여행 계획을 선택해주세요');
        return;
    }
    
    if (!title || !content) {
        alert('제목과 내용을 입력해주세요');
        return;
    }
    
    try {
        console.log('공유페이지: 게시글 작성 요청', { planId, title });
        const response = await fetch('/api/board/posts', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                planId: parseInt(planId),
                title: title,
                content: content,
                regionCode: regionCode || null,
                tripType: tripType || null,
                season: season || null,
                category: 'TRAVEL_PLAN'
            })
        });
        console.log('공유페이지: 게시글 작성 응답 상태:', response.status);
        const result = await response.json();
        console.log('공유페이지: 게시글 작성 결과:', result);
        
        if (result.success) {
            alert('게시글이 작성되었습니다!');
            window.location.href = 'board.html';
        } else {
            alert(result.message || '게시글 작성에 실패했습니다');
        }
    } catch (error) {
        console.error('공유 오류:', error);
        alert('공유 중 오류가 발생했습니다');
    }
});

// 여행 계획 선택 시 자동으로 정보 채우기
document.getElementById('planSelect')?.addEventListener('change', async (e) => {
    const planId = e.target.value;
    if (!planId) return;
    
    try {
        const token = localStorage.getItem('token');
        console.log('공유페이지: 선택된 계획 정보 요청', { planId });
        const response = await fetch(`/api/plans/${planId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('공유페이지: 계획 정보 응답 상태:', response.status);
        const result = await response.json();
        console.log('공유페이지: 계획 정보 결과:', result);
        
        if (result.success) {
            const plan = result.data.plan;
            if (!document.getElementById('postTitle').value) {
                document.getElementById('postTitle').value = plan.title || '';
            }
            if (!document.getElementById('postContent').value) {
                document.getElementById('postContent').value = `${plan.title} 여행 계획을 공유합니다.`;
            }
            if (plan.arrivalRegionCode) {
                document.getElementById('regionCode').value = plan.arrivalRegionCode;
            }
        }
    } catch (error) {
        console.error('계획 정보 로드 오류:', error);
    }
});

// 페이지 로드 시 여행 계획 목록 로드
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadPlans);
} else {
    loadPlans();
}

