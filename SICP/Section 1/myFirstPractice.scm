; (define x 1)
; (inspect (+ x x))

(define (main)
    (println "AUTHOR: George Corbin")
    )

; Problem 1
(println "Phone number: 1 (404) 491-4653")
(inspect (- 2 1))
(inspect (+ 400 4))
(inspect (- 500 9))
(inspect (* 1551 3))

; Problem 2
; ((((2 * 4) + (3 + 5) * 3) + ((10 - 7) + 6)) / 2)
(inspect (/ (+ (* 2 4) (* (+ 3 5) 3) (+ (- 10 7) 6)) 2))