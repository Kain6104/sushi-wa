document.addEventListener("DOMContentLoaded", function () {
    let timeLeft = 300; // 5 phút đếm ngược
    let progressBar = document.getElementById("paymentProgress");
    let timerText = document.getElementById("paymentTimer");
    let orderCode = document.getElementById("orderCode").innerText.trim();
    const totalPayable = parseInt(document.getElementById("totalPayable").innerText.trim(), 10);
    const changePaymentBtn = document.getElementById("changePaymentMethod");
    let paymentInterval;
    let isPaymentSuccessful = false; // Biến kiểm soát trạng thái thanh toán

	let paymentForm = document.getElementById("paymentForm"); // Lấy form thanh toán
	
	 let submitBtn = document.getElementById("submitBtn"); // Nút submit

	 // Vô hiệu hóa nút submit khi chọn VietQR
	 function checkSubmitStatus() {
	     let paymentMethod = document.getElementById("paymentMethod").value;
	     if (paymentMethod === "vietqr" && !isPaymentSuccessful) {
	         submitBtn.disabled = true;
	     } else {
	         submitBtn.disabled = false;
	     }
	 }
	 document.getElementById("paymentMethod").addEventListener("change", checkSubmitStatus);


    // Loại bỏ dấu '-' trong orderCode
    orderCode = orderCode.replace(/-/g, "");
    console.log("Order Code (đã loại bỏ '-'): ", orderCode);
    console.log("Total Payable:", totalPayable);

    function startCountdown() {
        let interval = setInterval(() => {
            if (timeLeft <= 0) {
                clearInterval(interval);
				clearInterval(paymentInterval);
				clearTimeout(); // Xóa mọi timeout

				$('#vietQrModal').modal('hide'); 
				$('#vietQrModal').removeClass('show');
				$('body').removeClass('modal-open');
				$('.modal-backdrop').remove();
                $('#vietQrModal').modal('hide');
                $('#paymentSuccessModal').modal('show');
                startRedirectCountdown();
            } else {
                let minutes = Math.floor(timeLeft / 60);
                let seconds = timeLeft % 60;
                progressBar.style.width = ((300 - timeLeft) / 300 * 100) + "%";
                timerText.innerText = `Đang chờ thanh toán ${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
                timeLeft--;
            }
        }, 1000);
    }

    function startRedirectCountdown() {
        let redirectTime = 5;
        let redirectBar = document.getElementById("redirectProgress");
        let redirectText = document.getElementById("redirectTimer");
        let interval = setInterval(() => {
            if (redirectTime <= 0) {
                clearInterval(interval);
				clearInterval(paymentInterval);
				clearTimeout(); // Xóa mọi timeout

				$('#vietQrModal').modal('hide'); 
				$('#vietQrModal').removeClass('show');
				$('body').removeClass('modal-open');
				$('.modal-backdrop').remove();
				$('#vietQrModal').modal('hide');
				$('#paymentSuccessModal').modal('hide');
            } else {
                redirectBar.style.width = ((5 - redirectTime) * 20) + "%";
                redirectText.innerText = `Bắt đầu sau ${redirectTime}s`;
                redirectTime--;
            }
        }, 1000);
    }

    async function checkPayment() {
        if (isPaymentSuccessful) {
            console.log("✅ Đã xác nhận thanh toán thành công, dừng kiểm tra!");
            return;
        }

        try {
            const response = await fetch('https://script.googleusercontent.com/macros/echo?user_content_key=eTh7pLpZtLttopvnwwNdHUqBwNn1hlvoRwNQvP8lx0uknL31BtYo1tCxiduc-jMJetUms_B2QButSEaxqzSjOoZRrgaSKy3tm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnKwWB-IM0KWIXlmS88CyE4Pcm2PgesiZFV65g6GorDGjXgd_NlsYupzbCePpq_53sQY_Xm43WAGVtgTrqXcaPr8HelkeFqengA&lib=MPj6U3WQnVq8gS_TM1jdTmX834RhQFocf');
            const data = await response.json();

            if (data.error || !data.data) {
                console.warn("⚠ Không tìm thấy dữ liệu giao dịch.");
                return;
            }

            const transactions = data.data;
            const paymentFound = transactions.some(tx =>
                tx["Mô tả"].includes(orderCode) && tx["Giá trị"] === totalPayable
            );

            if (paymentFound) {
                console.log("✅ Thanh toán thành công!");
                isPaymentSuccessful = true; // Đánh dấu trạng thái thanh toán thành công
                clearInterval(paymentInterval); // Dừng kiểm tra
				
				clearTimeout(); // Xóa mọi timeout

				$('#vietQrModal').modal('hide'); 
				$('#vietQrModal').removeClass('show');
				$('body').removeClass('modal-open');
				$('.modal-backdrop').remove();

                // Đảm bảo modal VietQR đã ẩn rồi mới hiện modal thành công
                setTimeout(() => {
                    $('#paymentSuccessModal').modal('show');
                    startRedirectCountdown();
                }, 500);
            } else {
                console.log("⏳ Chưa nhận được thanh toán, tiếp tục kiểm tra...");
            }
        } catch (error) {
            console.error("❌ Lỗi khi kiểm tra thanh toán:", error);
        }
		// Khi modal thanh toán thành công bị ẩn -> Submit form
		   $('#paymentSuccessModal').on('hidden.bs.modal', function () {
		       console.log("📌 Modal thanh toán thành công đã ẩn, tiến hành submit form...");

		       if (paymentForm) {
		           paymentForm.submit();
		       } else {
		           console.error("⚠ Không tìm thấy form thanh toán!");
		       }
		   });
    }

    $('#vietQrModal').on('shown.bs.modal', function () {
        startCountdown();
        paymentInterval = setInterval(checkPayment, 1000); // Kiểm tra mỗi 5 giây
    });

    changePaymentBtn.addEventListener("click", function () {
        if (confirm("Bạn có chắc chắn muốn đổi phương thức thanh toán không?")) {
            clearInterval(paymentInterval);
            $('#vietQrModal').modal('hide');
            location.reload(); // Load lại trang sau khi đổi phương thức thanh toán
        }
    });

    // Đảm bảo modal VietQR được đóng đúng cách
    $('#vietQrModal').on('hidden.bs.modal', function () {
        clearInterval(paymentInterval);
    });
});

