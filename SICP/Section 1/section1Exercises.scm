(println "AUTHOR: George Corbin")

(println " ")

(println "Exercise 1.2")
(inspect (/ (+ 5 4 (- 2 (- 3 (+ 6 (/ 4.0 5.0))))) (* 3 (- 6 2) (- 2 7))))
(println "The expected answer is -0.2466")
; (5 + 4 + (2 - (3 - (6 + (4 / 5))))) / (3 * (6 - 2) * (2 - 7)) = -0.2466

(println " ")

(println "Exercise 1.3")
(define (func x y z)
    (if (and (> x y) (> z y))
        (+ (* x x) (* z z))
        (if (and (> y x) (> z x))
            (+ (* y y) (* z z))
            (+ (* x x) (* y y))
    )))

(println (func 3 5 4))
(println "Expected Answer 41")

(println " ")

(println "Exercise 1.4")
(println "Everything whether an operator or a value is treated similarly and can be
the result of some other operation. In this case, if b is greater than zero, then the
result of the 'if' is '+', else the result is '-'.
Then that result is the operator that is used on 'a' and 'b'.
So if b>0, then it does '(+ a b)' [which is a+b],
else it does '(- a b)' [which is a-b],
so the the net result is 'a + |b|'.")
