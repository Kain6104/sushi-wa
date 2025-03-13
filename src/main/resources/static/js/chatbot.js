document.addEventListener("DOMContentLoaded", function () {
   const chatIcon = document.createElement("div");
   chatIcon.innerHTML = "💬";
   chatIcon.style.position = "fixed";
   chatIcon.style.bottom = "60px";
   chatIcon.style.right = "20px";
   chatIcon.style.width = "50px";
   chatIcon.style.height = "50px";
   chatIcon.style.backgroundColor = "#ff7f50";
   chatIcon.style.borderRadius = "50%";
   chatIcon.style.display = "flex";
   chatIcon.style.justifyContent = "center";
   chatIcon.style.alignItems = "center";
   chatIcon.style.cursor = "pointer";
   chatIcon.style.boxShadow = "0 4px 8px rgba(0,0,0,0.2)";
   chatIcon.style.zIndex = "1000";
   document.body.appendChild(chatIcon);

   const chatWindow = document.createElement("div");
   chatWindow.style.position = "fixed";
   chatWindow.style.bottom = "80px";
   chatWindow.style.right = "50px";
   chatWindow.style.width = "85%";
   chatWindow.style.maxWidth = "350px";
   chatWindow.style.height = "70vh";
   chatWindow.style.maxHeight = "500px";
   chatWindow.style.background = "white";
   chatWindow.style.borderRadius = "10px";
   chatWindow.style.boxShadow = "0 4px 8px rgba(0,0,0,0.2)";
   chatWindow.style.display = "none";
   chatWindow.style.flexDirection = "column";
   chatWindow.style.overflow = "hidden";
   chatWindow.style.zIndex = "1000";
   document.body.appendChild(chatWindow);

   chatIcon.addEventListener("click", function () {
       chatWindow.style.display = chatWindow.style.display === "none" ? "flex" : "none";
   });

   chatWindow.innerHTML = `
   <div style="background:#ff7f50;color:white;padding:10px;text-align:center;border-top-left-radius:10px;border-top-right-radius:10px;position:relative;display: flex; align-items: center;">
       <!-- Hình ảnh nằm ngoài cùng bên trái -->
       <img src="https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa_thumbnail.jpg" title="Chatbot" style="width:25px;height:25px;border-radius:50%;vertical-align:middle;margin-right:10px;" />
       <center>Chatbot </center>	
	   <a id="call-support" href="tel:0974802998" style="position:absolute;top:10px;right:30px;font-size:15px;color:white;cursor:pointer;">
	       <i class="fas fa-phone"></i>
	   </a>
       <p id="close-chat" style="position:absolute;top:10px;right:10px;font-size:20px;color:white;cursor:pointer;">&times;</p>
   </div>
	   <div id="chat-body" style="flex:1;padding:10px;overflow-y:auto;font-size:14px;">
	       <div style="display:flex; justify-content: flex-start; margin-bottom:10px;">
	           <img src="https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa_thumbnail.jpg" title="Chatbot" style="width:25px;height:25px;border-radius:50%;vertical-align:middle;margin-right:5px;" />
	           <div style="background:#ff7f50;color:white;padding:10px;border-radius:10px;max-width:60%;text-align:left;display:flex;align-items:center;">
	               <span>Xin chào! Tôi có thể giúp gì được cho bạn?<br>
				   Hotline: <a href="tel:0974802998">097 480 2998</a>
				   </span>
	           </div>
	       </div>
	   </div>
       <div id="suggestions" style="padding:10px;display:flex;flex-wrap:no-wrap;overflow-x:auto;width:100%;margin-bottom:10px;font-size:12px;"></div>
       <div style="display:flex;padding:10px;border-top:1px solid #ddd;font-size:12px;">
           <input id="chat-input" type="text" style="flex:1;padding:5px;border:1px solid #ddd;border-radius:5px;font-size:12px;" required>
           <button id="chat-send" style="margin-left:5px;padding:5px 10px;background:#ff7f50;color:white;border:none;border-radius:5px;font-size:12px;">Gửi</button>
       </div>
   `;
   
   // Lắng nghe sự kiện click trên biểu tượng dấu "X" để đóng chatbot
   const closeChatButton = document.getElementById("close-chat");

   closeChatButton.addEventListener("click", function () {
       chatWindow.style.display = "none"; // Ẩn cửa sổ chatbot
   });
   // Thêm sự kiện click vào hình ảnh để phóng to ảnh
    
   const chatBody = document.getElementById("chat-body");
   const chatInput = document.getElementById("chat-input");
   const suggestions = document.getElementById("suggestions");
   const chatSend = document.getElementById("chat-send");
   // Lắng nghe sự kiện click vào các ảnh trong chatbot
   chatBody.addEventListener("click", function(event) {
       if (event.target.tagName === "IMG") {
           const imageUrl = event.target.src;
           const allImages = Array.from(chatBody.querySelectorAll("img")); // Lấy tất cả hình ảnh trong chatBody
           const currentIndex = allImages.findIndex(img => img.src === imageUrl);
           openModal(imageUrl, allImages, currentIndex);
       }
   });

   // Hàm mở modal phóng to ảnh
   function openModal(imageUrl, allImages, currentIndex) {
       const modal = document.createElement("div");
       modal.style.position = "fixed";
       modal.style.top = "0";
       modal.style.left = "0";
       modal.style.width = "100%";
       modal.style.height = "100%";
       modal.style.backgroundColor = "rgba(0, 0, 0, 0.8)";
       modal.style.display = "flex";
       modal.style.justifyContent = "center";
       modal.style.alignItems = "center";
       modal.style.zIndex = "2000";
       modal.style.cursor = "pointer";
       modal.style.flexDirection = "column"; // Sắp xếp theo cột để hiển thị hình thu nhỏ dưới ảnh phóng to

       const modalImage = document.createElement("img");
       modalImage.src = imageUrl;
       modalImage.style.maxWidth = "90%";
       modalImage.style.maxHeight = "80%";
       modalImage.style.borderRadius = "10px";
       modal.appendChild(modalImage);

       // Tạo nút đóng "X"
       const closeButton = document.createElement("span");
       closeButton.innerHTML = "&times;";
       closeButton.style.position = "absolute";
       closeButton.style.top = "20px";
       closeButton.style.right = "20px";
       closeButton.style.fontSize = "30px";
       closeButton.style.color = "white";
       closeButton.style.cursor = "pointer";
       closeButton.style.zIndex = "3000";
       closeButton.addEventListener("click", function () {
           document.body.removeChild(modal);
       });

       modal.appendChild(closeButton);

       // Tạo phần thu nhỏ các ảnh phía dưới ảnh phóng to
       const thumbnailsContainer = document.createElement("div");
	   thumbnailsContainer.style.display = "flex";
	   thumbnailsContainer.style.marginTop = "10px";
	   thumbnailsContainer.style.overflowX = "auto"; // Cho phép cuộn ngang
	   thumbnailsContainer.style.padding = "10px";
	   thumbnailsContainer.style.justifyContent = "center"; // Căn giữa ảnh thu nhỏ
	   thumbnailsContainer.style.alignItems = "center"; // Căn giữa theo chiều dọc
	   thumbnailsContainer.style.maxWidth = "100%"; // Đảm bảo phần thu nhỏ không vượt ra ngoài
	   thumbnailsContainer.style.flexWrap = "nowrap"; // Không cho các ảnh thu nhỏ xuống hàng
	   thumbnailsContainer.style.justifyContent = "flex-start"; // Canh trái các ảnh thu nhỏ

       // Loại trừ hình ảnh của chatbot và user
       const filteredImages = allImages.filter(img => 
           img.src !== "https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa_thumbnail.jpg" &&
           img.src !== "https://cdn-icons-png.flaticon.com/512/10307/10307852.png"
       );

       filteredImages.forEach((img, index) => {
           const thumbnail = document.createElement("img");
           thumbnail.src = img.src;
           thumbnail.style.width = "60px";
           thumbnail.style.height = "60px";
           thumbnail.style.marginRight = "10px";
           thumbnail.style.cursor = "pointer";
           thumbnail.style.borderRadius = "5px";
           thumbnail.style.border = "2px solid transparent";
           thumbnail.style.transition = "border 0.3s";
           thumbnail.style.opacity = "0.5"; // Làm mờ các ảnh thu nhỏ chưa phóng to

           // Nếu ảnh đang phóng to, đặt opacity = 1
           if (index === currentIndex) {
               thumbnail.style.opacity = "1"; // Đảm bảo ảnh đang xem sáng hơn
               thumbnail.style.border = "1px solid red"; // Tạo viền đỏ cho ảnh
           }

           // Chọn ảnh thu nhỏ và cập nhật ảnh phóng to khi bấm vào ảnh thu nhỏ
           thumbnail.addEventListener("click", function (e) {
               e.stopPropagation(); // Ngăn không đóng modal khi click vào hình thu nhỏ

               // Cập nhật ảnh phóng to khi chọn ảnh thu nhỏ
               modalImage.src = img.src;
               currentIndex = index; // Cập nhật chỉ số của ảnh đang xem

               // Cập nhật độ sáng cho tất cả ảnh thu nhỏ
               filteredImages.forEach((otherImg, otherIndex) => {
                   const otherThumbnail = thumbnailsContainer.children[otherIndex];
                   if (otherIndex !== currentIndex) {
                       otherThumbnail.style.opacity = "0.5"; // Làm mờ ảnh khác
                       otherThumbnail.style.border = "2px solid transparent"; // Xóa viền
                   } else {
                       otherThumbnail.style.opacity = "1"; // Không làm mờ ảnh đã chọn
                       otherThumbnail.style.border = "1px solid red"; // Thêm viền đỏ cho ảnh đang mở
                   }
               });
           });

           // Thêm hiệu ứng border khi hover vào ảnh thu nhỏ
           thumbnail.addEventListener("mouseenter", function() {
               thumbnail.style.border = "2px solid #ff7f50"; // Chỉnh màu border khi hover
           });

           thumbnail.addEventListener("mouseleave", function() {
               if (thumbnail.style.opacity !== "1") {
                   thumbnail.style.border = "2px solid transparent"; // Đổi lại border ban đầu khi hover ra
               }
           });

           thumbnailsContainer.appendChild(thumbnail);
       });

       modal.appendChild(thumbnailsContainer);

       // Đóng modal khi click ngoài ảnh (không phải hình thu nhỏ)
       modal.addEventListener("click", function (event) {
           if (event.target !== modalImage && event.target !== thumbnailsContainer) {
               document.body.removeChild(modal);
           }
       });

       document.body.appendChild(modal);
   }


   // Các câu hỏi gợi ý ban đầu
   const initialQuestions = [
       "Menu của bạn là gì?",
       "Giá món sushi là bao nhiêu?",
       "Giờ mở cửa của cửa hàng là khi nào?",
       "Bạn có giao hàng không?"
   ];

   // Hiển thị các câu hỏi gợi ý
   function showSuggestions(questions) {
       suggestions.innerHTML = ''; // Xóa các câu hỏi cũ
       questions.forEach(question => {
           const button = document.createElement("button");
           button.innerText = question;
           button.style.margin = "5px";
           button.style.padding = "5px 10px";
           button.style.backgroundColor = "#ff7f50";
           button.style.color = "white";
           button.style.border = "none";
           button.style.borderRadius = "5px";
           button.style.cursor = "pointer";
           button.style.whiteSpace = "nowrap";  // Ngăn câu hỏi bị cắt ngang
           button.addEventListener("click", () => {
               chatInput.value = question; // Điền câu hỏi vào ô nhập
               sendMessage(question); // Gửi câu hỏi
           });
           suggestions.appendChild(button);
       });
   }

   
   // Hiển thị câu hỏi gợi ý ban đầu
   showSuggestions(initialQuestions);

   chatSend.addEventListener("click", function () {
       const userMessage = chatInput.value;
       if (!userMessage.trim()) return;
       sendMessage(userMessage);
   });

   // Hàm gửi câu hỏi tới backend và trả lời từ file JSON
   async function sendMessage(message) {
       const userMessageDiv = document.createElement("div");
       userMessageDiv.style.display = "flex";
       userMessageDiv.style.justifyContent = "flex-end";
       userMessageDiv.style.marginBottom = "10px";
       userMessageDiv.innerHTML = `
           <div style="background:#f1f0f0;padding:10px;border-radius:10px;max-width:60%;text-align:right;display:flex;align-items:center;">
               <span style="margin-left:10px;">${message}</span>
           </div>
           <img src="https://cdn-icons-png.flaticon.com/512/10307/10307852.png" title="Bạn" style="width:25px;height:25px;border-radius:50%;vertical-align:middle;margin-left:5px;" />
       `;
       chatBody.appendChild(userMessageDiv);
       chatInput.value = "";

       // Hiệu ứng "Đang trả lời..."
       const typingIndicator = document.createElement("div");
       typingIndicator.innerHTML = `
           <div style="display: flex; align-items: center; padding: 10px;">
               <img src="https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa_thumbnail.jpg" style="width:25px;height:25px;border-radius:50%;margin-right:10px;" />
               <em>Đang trả lời...</em>
           </div>
       `;
       chatBody.appendChild(typingIndicator);
	   try {
	       // Gửi tin nhắn đến API backend
	       const response = await fetch('/api/chatbot', {
	           method: 'POST',
	           headers: {
	               'Content-Type': 'application/json'
	           },
	           body: JSON.stringify({ message })
	       });

	       const data = await response.json();
	       chatBody.removeChild(typingIndicator); // Xóa hiệu ứng đang gõ

	       // Tạo thẻ chứa tin nhắn của chatbot
	       const botMessageDiv = document.createElement("div");
	       botMessageDiv.style.display = "flex";
	       botMessageDiv.style.justifyContent = "flex-start";
	       botMessageDiv.style.marginBottom = "10px";

	       // Avatar của chatbot
	       const botAvatar = document.createElement("img");
	       botAvatar.src = "https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa_thumbnail.jpg";
	       botAvatar.title = "Chatbot";
	       botAvatar.style.width = "25px";
	       botAvatar.style.height = "25px";
	       botAvatar.style.borderRadius = "50%";
	       botAvatar.style.verticalAlign = "middle";
	       botAvatar.style.marginRight = "5px";

	       // Khung chứa nội dung tin nhắn
	       const botMessageContent = document.createElement("div");
	       botMessageContent.style.background = "#ff7f50";
	       botMessageContent.style.color = "white";
	       botMessageContent.style.padding = "10px";
	       botMessageContent.style.borderRadius = "10px";
	       botMessageContent.style.maxWidth = "60%";
	       botMessageContent.style.textAlign = "left";
	       botMessageContent.style.whiteSpace = "pre-line";

	       // Xử lý định dạng tin nhắn trả về
	       let formattedReply;
	       if (data.reply.includes("* ")) {
	           formattedReply = data.reply
	               .replace(/\*{2}(.+?)\*{2}/g, '<strong>$1</strong>') // Chuyển **Tiêu đề** thành <strong>Tiêu đề</strong>
	               .replace(/\* (.+)/g, '<li>$1</li>') // Chuyển * mục thành danh sách <li>
	               .replace(/(<li>.*?<\/li>)/g, '<ul>$1</ul>'); // Gói danh sách trong <ul>
	       } else {
	           formattedReply = data.reply.replace(/\n/g, "<br>"); // Giữ xuống dòng với <br>
	       }

	       botMessageContent.innerHTML = formattedReply;
	       botMessageDiv.appendChild(botAvatar);
	       botMessageDiv.appendChild(botMessageContent);
	       chatBody.appendChild(botMessageDiv);

	   } catch (error) {
	       console.error('Lỗi khi gọi API:', error);
	       chatBody.removeChild(typingIndicator); // Xóa hiệu ứng nếu lỗi
	   }

   }

});
