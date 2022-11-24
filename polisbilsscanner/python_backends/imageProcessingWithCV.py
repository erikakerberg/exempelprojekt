import cv2
import pytesseract
import imutils

pytesseract.pytesseract.tesseract_cmd = r'C:\\Program Files\\Tesseract-OCR\\tesseract.exe'

img = cv2.imread('t2.jpg', cv2.IMREAD_COLOR)
img = imutils.resize(img, width = 500)
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
gray = cv2.bilateralFilter(gray, 11, 17, 17)
edged = cv2.Canny(gray, 30, 200)

cv2.imshow('test', gray)
cv2.waitKey(0)
cv2.destroyAllWindows()

text = pytesseract.image_to_string(gray)

print(text)