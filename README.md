#Trả lời caua hỏi

## Tại sao dùng Redis cho feed?
Redis là in-memory data store nhanh, hỗ trợ các cấu trúc dữ liệu như ZSet (sorted set) lý tưởng cho việc lưu trữ và truy vấn feed theo thứ tự thời gian giảm dần. Với lượng đọc nhiều hơn ghi, Redis đảm bảo hiệu suất cao cho việc lấy feed cũng như việc thêm post

## Feed được build theo hướng:
Push model (fanout on write): Khi tạo post, push postId vào feed của chính user và tất cả followers ngay lập tức. 
Ưu điểm: read feed nhanh. 
Nhược điểm: write có thể chậm nếu user có nhiều followers.

## Nếu user có 1 triệu follower, cách hiện tại có vấn đề gì?
Write post sẽ phải thực hiện 1 triệu tác vụ cho tổng các followr , dẫn đến thời gian xử lý dài, có thể gây timeout, overload Redis, và làm chậm hệ thống.

## Nếu feed quá lớn thì xử lý thế nào?
Sử dụng ZREMRANGEBYRANK để trim ZSet, chỉ giữ khoảng 200 posts mới nhất. Điều này giữ kích thước feed hợp lý, tránh memory bloat, và tập trung vào các post gần đây nhất.

## Giải thích lý do chọn structure của từng thành phần:

### Follow list sử dụng Redis Set:
Vì mỗi user cần không bị trùng lặp với các user khác, Set đảm bảo các phần tử là duy nhất và thao tác thêm/xóa/check được xử lý nhanh

### Post của user sử dụng Redis ZSet:
Cần sắp xếp bài post theo thời gian mới nhất trước nên ZSet phù hợp và ZSet tự động sắp xếp theo nên có thể lấy theo range mới nhất rất nhanh. 
Có thể dễ dàng trim để giới hạn số lượng post lưu lại của mỗi user.

### Feed của user sử dụng ZSet:
Feed cần hiển thị bài post mới nhất từ chính user và các user mà họ follow, sắp xếp theo thời gian giảm dần nên Zset là phù hợp. 
ZSet có thể tự động sắp xếp, lấy số post theo range và giới hạn kích thước feed tránh memory bloat.
Khi tạo post mới, ta có thể đẩy postId vào feed của chính user và tất cả follower ngay lập tức giúp việc đọc feed nhanh hơn

### Cache user feed: 
Lý do tương tự như feed của user và ZSet có thể giới hạn kích thước feed.
